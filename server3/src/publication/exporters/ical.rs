use anyhow::{Context, ensure};
use chrono::{DateTime, TimeDelta, Utc};

use super::{ExportFormat, ScheduleExporter};
use crate::publication::domain::ScheduleSnapshot;

pub struct IcalendarExporter;

impl ScheduleExporter for IcalendarExporter {
    fn format(&self) -> ExportFormat {
        ExportFormat {
            id: "ics", title: "Календарь · ICS", extension: "ics",
            description: "Обновляемая подписка или разовый импорт в сторонний календарь.",
            mime_type: "text/calendar; charset=utf-8", subscription: true,
        }
    }

    fn render(&self, snapshot: &ScheduleSnapshot) -> anyhow::Result<Vec<u8>> {
        ensure!(snapshot.timezone == "Europe/Moscow", "ICS adapter does not support this source timezone yet");
        let mut output = String::new();
        for line in ["BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//lavafrai//MAIapp publications 1//RU",
            "CALSCALE:GREGORIAN", "REFRESH-INTERVAL;VALUE=DURATION:PT1H", "X-PUBLISHED-TTL:PT1H"] {
            content_line(&mut output, line);
        }
        content_line(&mut output, &format!("NAME:{}", escape_text(&snapshot.name)));
        content_line(&mut output, &format!("X-WR-CALNAME:{}", escape_text(&snapshot.name)));
        content_line(&mut output, "X-WR-TIMEZONE:Europe/Moscow");
        for event in &snapshot.occurrences {
            let data = &event.data;
            let modified: DateTime<Utc> = DateTime::from_timestamp(event.modified_at, 0)
                .context("invalid event modification timestamp")?;
            // Sources currently use Moscow civil time (2015 onwards). Emit UTC
            // rather than floating times; no client timezone or DST guessing.
            let start = data.date.and_time(data.start) - TimeDelta::hours(3);
            let end = data.date.and_time(data.end) - TimeDelta::hours(3);
            let title = if data.kind.is_empty() { data.title.clone() }
                else { format!("{} ({})", data.title, data.kind) };
            let mut description = Vec::new();
            if !data.teachers.is_empty() {
                description.push(format!("Преподаватели: {}", data.teachers.iter()
                    .map(|p| p.name.as_str()).collect::<Vec<_>>().join(", ")));
            }
            if !data.groups.is_empty() { description.push(format!("Группы: {}", data.groups.join(", "))); }
            description.extend(data.links.iter().cloned());
            for line in [
                "BEGIN:VEVENT".to_owned(),
                format!("UID:{}", event.uid),
                format!("DTSTAMP:{}", modified.format("%Y%m%dT%H%M%SZ")),
                format!("LAST-MODIFIED:{}", modified.format("%Y%m%dT%H%M%SZ")),
                format!("SEQUENCE:{}", event.sequence),
                format!("DTSTART:{}", start.format("%Y%m%dT%H%M%SZ")),
                format!("DTEND:{}", end.format("%Y%m%dT%H%M%SZ")),
                format!("SUMMARY:{}", escape_text(&title)),
                format!("LOCATION:{}", escape_text(&data.rooms.iter().map(|r| r.name.as_str())
                    .collect::<Vec<_>>().join(", "))),
                format!("DESCRIPTION:{}", escape_text(&description.join("\n"))),
                format!("STATUS:{}", if event.cancelled { "CANCELLED" } else { "CONFIRMED" }),
                format!("TRANSP:{}", if event.cancelled { "TRANSPARENT" } else { "OPAQUE" }),
                "END:VEVENT".to_owned(),
            ] { content_line(&mut output, &line); }
        }
        content_line(&mut output, "END:VCALENDAR");
        Ok(output.into_bytes())
    }
}

/// RFC 5545 section 3.3.11. Input can never create a new ICS property.
pub fn escape_text(input: &str) -> String {
    input.replace('\\', "\\\\").replace("\r\n", "\n").replace('\r', "\n")
        .replace('\n', "\\n").replace(';', "\\;").replace(',', "\\,")
}

/// RFC 5545 section 3.1: 75 *octets*, including the continuation space, and no
/// splitting a UTF-8 code point. Every physical line uses CRLF.
pub fn content_line(output: &mut String, line: &str) {
    let mut octets = 0;
    for c in line.chars() {
        if octets + c.len_utf8() > 75 {
            output.push_str("\r\n ");
            octets = 1;
        }
        output.push(c);
        octets += c.len_utf8();
    }
    output.push_str("\r\n");
}
