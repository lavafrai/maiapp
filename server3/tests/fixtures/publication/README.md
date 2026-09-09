# Publication fixtures

`group.json` is a **synthetic**, deterministic fixture matching the current
`/api/v1/schedule/{id}` response model. It is not a downloaded MAI schedule.
Names and room identifiers are deliberately fictitious.

Live data was not reachable from the implementation environment. The ignored
`live_public_api_smoke` Rust test can exercise real API responses explicitly;
normal unit tests have no network dependency.
