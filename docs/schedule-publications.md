# Schedule publications and export

## Scope and public interface

One implicit public publication exists per canonical MAI group identifier or
teacher UUID. Source data are public; there is no account, upload endpoint or
publication of local app events, annotations or personal credentials.

The landing page `/export` discovers format metadata, searches public schedules,
checks an export with HEAD, then provides a subscription URL and a separate
one-time download. The home page links to it. The Kotlin app and its JSON API
are unchanged by this implementation.

Endpoints below are relative to the server; the deployment prefixes `/api/v1`:

| Endpoint | Result |
| --- | --- |
| `GET /exports` | Array of registered format descriptors |
| `GET /exports/group/{group-name}/ics` | Persistent public calendar subscription |
| `GET /exports/teacher/{teacher-uuid}/ics` | Teacher calendar, identified by UUID, not name |
| `GET .../ics?download=true` | Same calendar representation with attachment disposition |
| `HEAD .../ics` | Same preparation/headers as GET, no response body |

Encode the identifier once as a UTF-8 URL path segment. Axum decodes it once;
the publication handler does not apply the old API's additional decoder.
Invalid source identifiers return 400; unknown formats 404. First-publication
failures return **503 JSON, `Retry-After: 60`, `Cache-Control: no-store`**, never
an empty successful calendar. Busy cold requests also return 503 instead of
building a second publication concurrently.

Published responses include the MIME type, safe ASCII filename, `ETag`,
`Last-Modified`, `X-MAIapp-Source-Observed-At` (Unix seconds), and
`X-MAIapp-Publication-State` (`current`, `stale`, `held`). `If-None-Match`
supports weak validators, lists and `*`; an unchanged representation returns
304. `If-Modified-Since` is not implemented. A source check that changes
nothing does not change the artifact bytes, event timestamps or ETag.

The web page defaults to same-origin `/api/v1`. For a separate development
server set `VITE_MAIAPP_API_BASE`, and include the development origin in the
Rust server's existing CORS configuration. The API base must not contain
credentials, query parameters or a fragment.

## Domain boundaries

```
MAI repository -> source adapter / normalization -> exact ScheduleSnapshot
                                                     |
                                      identity reconciliation + publication
                                                     |
                                           ScheduleExporter registry
                                                     |
                                      last-good artifacts + atomic storage
```

`OccurrenceData` records actual dates and times, separate teacher and group
collections, rooms, links, title and type. The adapter corrects the legacy
teacher-response convention that puts group names in `lectors`. It sorts
metadata for stability but never deduplicates occurrences. `Coverage` describes
only the dates observed in the response, and explicitly has `complete = false`.
It does not assert that MAI supplied a complete semester.

`ScheduleExporter::render` is a pure transformation: no networking, publication
policy, disk access or recurrence inference. Register a new statically linked
adapter with `ExportRegistry::register` before sharing the service. The registry
validates identifiers, filename extensions, MIME headers and uniqueness. Its
metadata drives the existing format selector; a download-only format does not
show calendar subscription actions. There is no dynamic code/plugin loader.

Each successful format is stored with its own snapshot revision. A failing
future image/Excel exporter retains its own last-good artifact while ICS can
advance. If every exporter fails, the snapshot is not replaced. A newly
registered format can render the persisted snapshot without upstream access.
Interactive web pages can later read the snapshot rather than pretending to be
binary exporters; this PR does not add a snapshot API or immutable version URLs.

## Update, failure and deletion policy

Existing subscribers get the stored artifact immediately, without waiting for
MAI. A request starts a bounded asynchronous refresh when the last successful
check is at least 15 minutes old. There is no always-running polling job for
unused publications. Public HTTP caching lasts five minutes; clients choose
their own polling schedules, so urgent changes are not guaranteed to arrive
within five or fifteen minutes. A warm request may return the previous version
while its refresh completes; a subsequent poll observes the replacement.

Four global refresh workers and 64 fixed single-writer stripes bound concurrent
work without an unbounded per-user lock map. Hash collisions can defer refresh,
not mix publications. Failed attempts back off for a minute. Source fetching
has a 40-second timeout. A cancelled first HTTP request cannot cancel the
publication commit and release its writer gate prematurely.

Validation rejects empty schedules, mismatched group identity, inconsistent or
duplicate dates, invalid/end-before-start times, unsupported control characters
and excessive input sizes. A failed fetch, parse, validation, render or
pre-commit disk write leaves the old publication available. A valid-looking
but semantically incorrect source is not perfectly detectable; this is a
conservative safety policy, not a proof of upstream completeness.

An absent old occurrence **outside** the new response's observed date bounds is
retained and marked as such. It is not treated as an announced cancellation.
Within those bounds, removals are held until the same candidate is observed in
two independently fetched source versions at least 15 minutes apart. The adapter
uses the repository's `Schedule.created` timestamp, which is set on a real parse,
so repeatedly reading one cached response cannot confirm a removal. Pending
state survives a restart. While pending, the entire old snapshot is served;
additions and unrelated edits in that candidate also wait for confirmation.

A drop of **more than 80%** the active occurrences within the observed window
is rejected, including on repeated fetches. Empty calendars are never accepted.
This can deliberately hold a genuine broad cancellation or withdrawal. There
is no unsafe unauthenticated override endpoint. Operator-approved bulk-deletion
and authoritative-coverage policies are follow-up work; do not delete the
publication file to force acceptance, because that discards UID history.
Operational logs record held removals, failed refreshes and exporter failures.

Confirmed removed events retain their UID and increment their sequence with
`STATUS:CANCELLED`, rather than simply disappearing. Tombstones and occurrences
outside current coverage are retained. There is no automatic history pruning.
The initial bounds are 4,096 incoming and 16,384 stored occurrences, 8 MiB per
artifact and 32 MiB per stored record. Reaching a bound holds the previous
publication; monitor disk use and plan an explicit retention/migration policy
before long-lived publications grow to these limits.

## Event identity and ICS

The source has no persistent lesson ID. Exact matches preserve multiplicity.
Otherwise only unambiguous one-to-one matches are used: date/start/title/type/
groups, then date/start/groups, then date/title/type/groups for a same-day time
move. Ambiguous simultaneous edits are not arbitrarily merged. Cross-date moves
are represented as a new event plus a conservatively confirmed cancellation
(or a retained old event when outside coverage), not a guessed UID transfer.

Assigned UIDs, per-event sequence and modification time live in the persisted
snapshot. Metadata changes and recognized moves retain identity; unchanged
events keep their timestamps. A restored cancellation can reactivate its UID.
Deleting storage, starting a new independent installation, or changing a source
identifier is not an identity-preserving migration.

ICS exports one VEVENT for each concrete occurrence. It uses CRLF, UTF-8 line
folding at 75 octets without splitting code points, escaped TEXT properties,
UTC DTSTART/DTEND, stable DTSTAMP/LAST-MODIFIED, UID and SEQUENCE. No guessed
RRULE, invitation METHOD or forced alarms are emitted. The refresh hint is one
hour, not an enforcement mechanism for third-party clients.

Current sources are explicitly Moscow civil time. The initial adapter accepts
academic dates in 2015–2100 and converts using the currently applicable fixed
UTC+03:00 rule. It is not a general IANA timezone engine: other zones or future
changes to Moscow civil-time rules require updating the adapter/policy before
publishing affected schedules. Historical pre-2015 data are rejected rather
than silently assigned an incorrect offset.

## Persistence and deployment

`MAIAPP_PUBLICATIONS_DIR` defaults to
`/var/lib/maiapp-server3/publications`. The existing Compose volume
`server3-telemetry:/var/lib/maiapp-server3` already persists this directory;
no additional volume is needed. Do not put it under the disposable API cache
or run cache cleanup against it. Back it up with the rest of application data.

A record commits the normalized snapshot and last-good artifacts together:
temporary file in the same directory, file fsync, atomic rename, then directory
fsync on Unix. Files are named by a non-secret content-address hash of the
canonical source key; user input is never a path. Artifact bytes are base64 in
JSON. Checksums detect accidental damage, not malicious tampering; filesystem
ownership/permissions remain the trust boundary. Only a single server process
may write a publication directory. Multiple replicas need an inter-process
lock/transactional store, not this in-process stripe lock.

Unknown schema versions, corrupt JSON, invalid UID history and invalid artifact
checksums are not overwritten with a fresh publication. They return an error;
restore a backup or write an explicit migration. An error after a successful
atomic rename can mean the new complete record is present; no partial record
is intentionally made visible. Network filesystems with weaker rename/fsync
semantics are not supported by this initial store.

Deploy the server image, rebuilt landing and proxy changes together. The Caddy
exception permits only GET/HEAD document paths under
`/api/v1/exports/(group|teacher)/...`; the rest of the site remains behind Anubis.
Nginx preserves the `/exports/` portion when stripping `/api/v1`, disables
proxy caching and applies an aggregate 20 requests/second budget with a burst
of 80, returning 429 on excess. This is a global budget, not per-client fairness:
it deliberately does not trust arbitrary forwarding headers. Tune it against
actual subscriber traffic; it is not a comprehensive edge DoS defense.

Validate Caddy with the project's custom image because its existing `usage`
directive is not in stock Caddy. No production deployment is performed by the
new check workflow.

## Future university-style representations

Introduce a presentation builder *between* exact occurrences and adapters.
It may group by weekday, period, date interval and academic-week parity, with
explicit exceptions. Academic week numbering needs a trustworthy semester
anchor; ISO week parity is not a substitute. If context or regularity is missing,
keep explicit dates.

The required invariant is a multiset round trip:

```
expand(presentation, academic_calendar) == exact_occurrences_in_selected_period
```

Compare complete event payloads and multiplicities, not just dates or counts.
Different rooms, teachers, groups, durations and simultaneous lessons must not
be lost. Never extend an inferred pattern beyond known coverage. Excel, images
and static HTML can share this builder without changing ICS identity. This
implementation intentionally includes no recurrence compressor, Excel adapter,
image renderer, personal upload or blank future-format buttons.

## Verification

From a complete checkout with the patch applied:

```sh
(cd server3 && cargo test --locked)
(cd landing && npm ci && node --experimental-strip-types --test tests/export.test.mjs && npm run build)
# Optional external dependency: deliberately not run in deterministic CI.
(cd server3 && cargo test --locked live_public_api_smoke -- --ignored --nocapture)
```

The new workflow runs the first two checks on relevant PRs. Rust tests cover
normalization, teacher mapping, multiplicity, identity/version stability,
confirmation across restart, UTC rollover, TEXT injection resistance, UTF-8
folding, atomic storage validation, stale/cold failures, HTTP headers/304/HEAD,
concurrent cold requests, cancelled requests and independent exporter failure.
The checked-in schedule is synthetic, not a claimed live API download.

Before deployment, additionally verify real calendar clients and desktop/mobile
browser flows, clipboard fallback, failed discovery, teacher search, public
challenge-free feed access and proxy throttling. Compilation alone cannot
establish Google/Apple/Outlook refresh latency or client deletion behavior.

Standards and implementation references:
- RFC 5545, especially sections 3.1, 3.3.11 and VEVENT/UID/SEQUENCE:
  https://www.rfc-editor.org/rfc/rfc5545.html
- RFC 7986, REFRESH-INTERVAL: https://www.rfc-editor.org/rfc/rfc7986.html
- RFC 9110, conditional requests: https://www.rfc-editor.org/rfc/rfc9110.html
- Caddy matchers: https://caddyserver.com/docs/caddyfile/matchers
- Nginx request limits: https://nginx.org/en/docs/http/ngx_http_limit_req_module.html
