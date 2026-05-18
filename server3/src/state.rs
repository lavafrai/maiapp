use std::sync::Arc;

use crate::{
    services::{data::MaiDataService, exler::ExlerService, schedule::ScheduleService},
    telemetry::Telemetry,
};

#[derive(Clone)]
pub struct AppState {
    pub schedule: Arc<ScheduleService>,
    pub exler: Arc<ExlerService>,
    pub data: Arc<MaiDataService>,
    pub telemetry: Arc<Telemetry>,
}

impl AppState {
    pub fn new(
        schedule: Arc<ScheduleService>,
        exler: Arc<ExlerService>,
        data: Arc<MaiDataService>,
        telemetry: Arc<Telemetry>,
    ) -> Self {
        Self {
            schedule,
            exler,
            data,
            telemetry,
        }
    }
}
