//! Exact occurrences are the source of truth. Recurrence inference belongs to a
//! future, independently verified presentation layer, never to publication.
use std::collections::{BTreeMap, BTreeSet};

use anyhow::{Context, ensure};
use chrono::{Datelike, NaiveDate, NaiveTime, Timelike};
use serde::{Deserialize, Serialize};

use crate::models::Schedule;

pub const SCHEMA_VERSION: u32 = 1;
pub const MAX_OCCURRENCES: usize = 16_384;

#[derive(Clone, Copy, Debug, Serialize, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "snake_case")]
pub enum SourceKind {
    Group,
    Teacher,
}

#[derive(Clone, Debug, Serialize, Deserialize, PartialEq, Eq)]
pub struct Source {
    pub kind: SourceKind,
    pub id: String,
}

impl Source {
    pub fn parse(kind: &str, id: &str) -> anyhow::Result<Self> {
        ensure!(id.len() <= 200, "schedule identifier is too long");
        let (kind, pattern) = match kind {
            "group" => (SourceKind::Group, r"^(([МТ])([\dИУ]+?)([ОВЗ]))-((\d+?)(Б|С|А|СВ|БВ|М)к?и?)-(\d+?)$"),
            "teacher" => (SourceKind::Teacher, r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
            _ => anyhow::bail!("unsupported source kind"),
        };
        ensure!(regex::Regex::new(pattern)?.is_match(id), "invalid schedule identifier");
        Ok(Self { kind, id: id.to_owned() })
    }

    pub fn kind_name(&self) -> &'static str {
        match self.kind {
            SourceKind::Group => "group",
            SourceKind::Teacher => "teacher",
        }
    }

    pub fn key(&self) -> String {
        format!("{}:{}", self.kind_name(), self.id)
    }
}

#[derive(Clone, Debug, Serialize, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct Participant {
    pub id: String,
    pub name: String,
}

#[derive(Clone, Debug, Serialize, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct OccurrenceData {
    pub date: NaiveDate,
    pub start: NaiveTime,
    pub end: NaiveTime,
    pub title: String,
    pub kind: String,
    pub teachers: Vec<Participant>,
    pub groups: Vec<String>,
    pub rooms: Vec<Participant>,
    pub links: Vec<String>,
}

#[derive(Clone, Debug, Serialize, Deserialize, PartialEq, Eq)]
pub struct Occurrence {
    pub uid: String,
    pub sequence: u32,
    pub modified_at: i64,
    pub cancelled: bool,
    /// Absence outside the new observation window is not evidence of deletion.
    pub retained_outside_coverage: bool,
    pub data: OccurrenceData,
}

#[derive(Clone, Debug, Serialize, Deserialize, PartialEq, Eq)]
pub struct Coverage {
    pub from: NaiveDate,
    pub through: NaiveDate,
    /// These are observed bounds, not an assertion of semester completeness.
    pub complete: bool,
}

#[derive(Clone, Debug, Serialize, Deserialize, PartialEq, Eq)]
pub struct ScheduleSnapshot {
    pub source: Source,
    pub name: String,
    pub timezone: String,
    pub revision: u64,
    pub coverage: Coverage,
    pub occurrences: Vec<Occurrence>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct PendingRemoval {
    pub fingerprint: String,
    pub first_observed_at: i64,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct PublicationState {
    pub snapshot: ScheduleSnapshot,
    pub last_checked_at: i64,
    pub source_observed_at: i64,
    pub pending: Option<PendingRemoval>,
}

#[derive(Clone, Debug, Serialize)]
pub struct Candidate {
    pub source: Source,
    pub name: String,
    pub coverage: Coverage,
    pub occurrences: Vec<OccurrenceData>,
    #[serde(skip)]
    pub observed_at: i64,
}

pub fn digest(value: impl AsRef<[u8]>) -> String {
    // Non-secret content addressing, not authentication or an integrity signature.
    format!("{:x}", md5::compute(value))
}

fn checked_text(value: &str) -> anyhow::Result<String> {
    ensure!(value.len() <= 16_384, "text field is too large");
    ensure!(
        !value.chars().any(|c| c.is_control() && !matches!(c, '\n' | '\r' | '\t')),
        "unsupported control character"
    );
    Ok(value.trim().to_owned())
}

fn parse_time(value: &str) -> anyhow::Result<NaiveTime> {
    let time = NaiveTime::parse_from_str(value, "%H:%M:%S")
        .or_else(|_| NaiveTime::parse_from_str(value, "%H:%M"))
        .context("invalid lesson time")?;
    ensure!(time.nanosecond() == 0, "fractional seconds and leap seconds are not supported");
    Ok(time)
}

pub fn normalize(source: Source, schedule: Schedule) -> anyhow::Result<Candidate> {
    ensure!(Source::parse(source.kind_name(), &source.id)? == source, "non-canonical source");
    let name = checked_text(&schedule.name)?;
    ensure!(!name.is_empty(), "schedule name is empty");
    if source.kind == SourceKind::Group {
        ensure!(name == source.id, "source returned a different group");
    }
    ensure!(schedule.days.len() <= 1500, "too many schedule days");
    let mut dates = BTreeSet::new();
    let mut occurrences = Vec::new();
    for day in schedule.days {
        ensure!(dates.insert(day.date), "duplicate schedule day");
        // Moscow has UTC+03:00 throughout this supported academic date range.
        ensure!((2015..=2100).contains(&day.date.year()), "unsupported academic date");
        for lesson in day.lessons {
            ensure!(lesson.day == day.date, "lesson date disagrees with its day");
            let start = parse_time(&lesson.time_start.time)?;
            let end = parse_time(&lesson.time_end.time)?;
            ensure!(end > start, "lesson must end after it starts");
            let title = checked_text(&lesson.name)?;
            ensure!(!title.is_empty(), "lesson title is empty");
            let mut teachers = Vec::new();
            let mut groups = Vec::new();
            // The legacy teacher API puts group names in `lectors`.
            for lector in lesson.lectors {
                if source.kind == SourceKind::Teacher {
                    groups.push(checked_text(&lector.name.name)?);
                } else {
                    teachers.push(Participant {
                        id: checked_text(&lector.uid.uid)?,
                        name: checked_text(&lector.name.name)?,
                    });
                }
            }
            if source.kind == SourceKind::Group {
                groups.push(source.id.clone());
            } else {
                teachers.push(Participant { id: source.id.clone(), name: name.clone() });
            }
            let mut rooms = lesson.rooms.into_iter().map(|room| {
                Ok(Participant { id: checked_text(&room.uid)?, name: checked_text(&room.name)? })
            }).collect::<anyhow::Result<Vec<_>>>()?;
            let mut links = [lesson.lms, lesson.teams, lesson.other].iter()
                .map(|s| checked_text(s)).collect::<anyhow::Result<Vec<_>>>()?;
            links.retain(|s| !s.is_empty());
            teachers.sort();
            groups.sort();
            rooms.sort();
            links.sort();
            occurrences.push(OccurrenceData {
                date: day.date, start, end, title, kind: checked_text(&lesson.lesson_type)?,
                teachers, groups, rooms, links,
            });
            ensure!(occurrences.len() <= 4096, "too many source occurrences");
        }
    }
    ensure!(!occurrences.is_empty(), "empty schedule is not safe to publish");
    occurrences.sort(); // Preserve multiplicity; never deduplicate lessons.
    Ok(Candidate {
        source, name, occurrences,
        coverage: Coverage {
            from: *dates.first().context("no observed dates")?,
            through: *dates.last().context("no observed dates")?,
            complete: false,
        },
        observed_at: schedule.created,
    })
}

fn match_keys<K: Ord>(
    new: &[OccurrenceData], old: &[Occurrence], matches: &mut [Option<usize>],
    key: impl Fn(&OccurrenceData) -> K, unique_only: bool,
) {
    let used: BTreeSet<_> = matches.iter().flatten().copied().collect();
    let mut left: BTreeMap<K, Vec<usize>> = BTreeMap::new();
    let mut right: BTreeMap<K, Vec<usize>> = BTreeMap::new();
    for (i, item) in new.iter().enumerate() {
        if matches[i].is_none() { left.entry(key(item)).or_default().push(i); }
    }
    for (i, item) in old.iter().enumerate() {
        if !used.contains(&i) { right.entry(key(&item.data)).or_default().push(i); }
    }
    for (key, new_ids) in left {
        if let Some(old_ids) = right.get(&key) {
            if unique_only && (new_ids.len() != 1 || old_ids.len() != 1) { continue; }
            for (new_id, old_id) in new_ids.iter().zip(old_ids) {
                matches[*new_id] = Some(*old_id);
            }
        }
    }
}

/// Reconcile identity independently of any exporter. Uncertain cross-date moves
/// are a cancellation/new occurrence, never a guessed identity match.
pub fn reconcile(
    previous: Option<&PublicationState>, candidate: Candidate, now: i64,
    confirmation_seconds: i64,
) -> anyhow::Result<PublicationState> {
    ensure!(candidate.observed_at > 0 && candidate.observed_at <= now + 300, "invalid source observation time");
    let old = previous.map(|s| s.snapshot.occurrences.as_slice()).unwrap_or(&[]);
    if let Some(previous) = previous {
        ensure!(previous.snapshot.source == candidate.source, "publication source changed");
        ensure!(candidate.observed_at >= previous.source_observed_at, "source observation went backwards");
    }
    let mut matches = vec![None; candidate.occurrences.len()];
    match_keys(&candidate.occurrences, old, &mut matches, |d| d.clone(), false);
    match_keys(&candidate.occurrences, old, &mut matches,
        |d| (d.date, d.start, d.title.clone(), d.kind.clone(), d.groups.clone()), true);
    match_keys(&candidate.occurrences, old, &mut matches,
        |d| (d.date, d.start, d.groups.clone()), true);
    match_keys(&candidate.occurrences, old, &mut matches,
        |d| (d.date, d.title.clone(), d.kind.clone(), d.groups.clone()), true);
    let used: BTreeSet<_> = matches.iter().flatten().copied().collect();
    let covered = |d: &OccurrenceData| d.date >= candidate.coverage.from && d.date <= candidate.coverage.through;
    let removed: Vec<_> = old.iter().enumerate().filter(|(i, o)|
        !used.contains(i) && !o.cancelled && covered(&o.data)
    ).map(|(i, _)| i).collect();
    if !removed.is_empty() {
        let active_in_window = old.iter().filter(|o| !o.cancelled && covered(&o.data)).count();
        // Exactly 80% can be confirmed; only a larger drop is rejected.
        ensure!(removed.len() * 100 <= active_in_window * 80, "suspicious mass removal; keep last-good publication");
        let fingerprint = digest(serde_json::to_vec(&candidate)?);
        let pending = previous.and_then(|p| p.pending.as_ref());
        let confirmed = pending.is_some_and(|p|
            p.fingerprint == fingerprint && candidate.observed_at > p.first_observed_at
                && candidate.observed_at - p.first_observed_at >= confirmation_seconds
        );
        if !confirmed {
            let mut state = previous.context("removals require an existing publication")?.clone();
            let first_observed_at = pending.filter(|p| p.fingerprint == fingerprint)
                .map(|p| p.first_observed_at).unwrap_or(candidate.observed_at);
            state.pending = Some(PendingRemoval { fingerprint, first_observed_at });
            state.last_checked_at = now;
            state.source_observed_at = candidate.observed_at;
            return Ok(state);
        }
    }
    let revision = previous.map(|s| s.snapshot.revision).unwrap_or(0)
        .checked_add(1).context("publication revision overflow")?;
    let mut occurrences = Vec::new();
    for (index, data) in candidate.occurrences.into_iter().enumerate() {
        if let Some(old_id) = matches[index] {
            let mut item = old[old_id].clone();
            if item.data != data || item.cancelled {
                item.sequence = item.sequence.checked_add(1).context("event sequence overflow")?;
                item.modified_at = now.max(item.modified_at.checked_add(1).context("event timestamp overflow")?);
            }
            item.cancelled = false;
            item.retained_outside_coverage = false;
            item.data = data;
            occurrences.push(item);
        } else {
            let identity = serde_json::to_vec(&(candidate.source.key(), revision, index, &data))?;
            occurrences.push(Occurrence {
                uid: format!("{}@maiapp.lavafrai.ru", digest(identity)),
                sequence: 0, modified_at: now, cancelled: false,
                retained_outside_coverage: false, data,
            });
        }
    }
    for (i, old_item) in old.iter().enumerate() {
        if used.contains(&i) { continue; }
        let mut item = old_item.clone();
        if removed.contains(&i) {
            item.cancelled = true;
            item.sequence = item.sequence.checked_add(1).context("event sequence overflow")?;
            item.modified_at = now.max(item.modified_at.checked_add(1).context("event timestamp overflow")?);
        }
        item.retained_outside_coverage = !covered(&item.data);
        occurrences.push(item);
    }
    ensure!(occurrences.len() <= MAX_OCCURRENCES, "publication history limit reached");
    occurrences.sort_by(|a, b| a.data.cmp(&b.data).then(a.uid.cmp(&b.uid)));
    let mut snapshot = ScheduleSnapshot {
        source: candidate.source, name: candidate.name, timezone: "Europe/Moscow".to_owned(),
        revision, coverage: candidate.coverage, occurrences,
    };
    if let Some(previous) = previous {
        let mut comparable = snapshot.clone();
        comparable.revision = previous.snapshot.revision;
        if comparable == previous.snapshot { snapshot.revision = previous.snapshot.revision; }
    }
    Ok(PublicationState {
        snapshot, pending: None, last_checked_at: now, source_observed_at: candidate.observed_at,
    })
}
