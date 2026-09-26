use std::sync::Arc;

use crate::{
    publication::service::PublicationService,
    services::{data::MaiDataService, exler::ExlerService, schedule::ScheduleService},
    telemetry::Telemetry,
};

#[derive(Clone)]
pub struct AppState {
    pub schedule: Arc<ScheduleService>,
    pub exler: Arc<ExlerService>,
    pub data: Arc<MaiDataService>,
    pub telemetry: Arc<Telemetry>,
    pub publications: Arc<PublicationService>,
}

impl AppState {
    pub fn new(
        schedule: Arc<ScheduleService>,
        exler: Arc<ExlerService>,
        data: Arc<MaiDataService>,
        telemetry: Arc<Telemetry>,
        publications: Arc<PublicationService>,
    ) -> Self {
        Self {
            schedule,
            exler,
            data,
            telemetry,
            publications,
        }
    }
}
