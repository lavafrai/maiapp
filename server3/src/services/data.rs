use crate::models::{Asset, MaiDataItem, MaiDataManifest};

pub struct MaiDataService;

impl MaiDataService {
    pub fn new() -> Self {
        Self
    }

    pub fn manifest(&self) -> MaiDataManifest {
        MaiDataManifest {
            version: 1,
            data: vec![
                builtin(
                    "Расписания преподавателей",
                    "Преподаватели",
                    "assets/icons/users.svg",
                    "teacher-schedule",
                ),
                builtin(
                    "О преподавателях",
                    "Преподаватели",
                    "assets/icons/message-square.svg",
                    "teacher-review",
                ),
                item(
                    "map",
                    "План студгородка",
                    "Студгородок",
                    "assets/icons/map.svg",
                    Asset::Web {
                        text: "images/MAIapp3_day_2025-05.png",
                    },
                    Some(Asset::Web {
                        text: "images/MAIapp3_night_2025-05.png",
                    }),
                ),
                item(
                    "web",
                    "Столовые и буфеты",
                    "Студгородок",
                    "assets/icons/coffee.svg",
                    Asset::Web {
                        text: "markup/cafeterias.html",
                    },
                    None,
                ),
                item(
                    "web",
                    "Библиотеки",
                    "Студгородок",
                    "assets/icons/book-open.svg",
                    Asset::Web {
                        text: "markup/libraries.html",
                    },
                    None,
                ),
                item(
                    "web",
                    "Спортивные секции",
                    "Жизнь",
                    "assets/icons/biking-solid.svg",
                    Asset::Web {
                        text: "markup/sport_sections.html",
                    },
                    None,
                ),
                item(
                    "web",
                    "Маёвский словарик",
                    "Жизнь",
                    "assets/icons/book-solid.svg",
                    Asset::Web {
                        text: "markup/exlers_dictionary.html",
                    },
                    None,
                ),
                item(
                    "web",
                    "Творческие коллективы",
                    "Жизнь",
                    "assets/icons/palette.svg",
                    Asset::Web {
                        text: "markup/creative_teams.html",
                    },
                    None,
                ),
                item(
                    "web",
                    "Студенческие организации",
                    "Жизнь",
                    "assets/icons/user-friends.svg",
                    Asset::Web {
                        text: "markup/students_organizations.html",
                    },
                    None,
                ),
            ],
        }
    }
}

fn builtin(
    name: &'static str,
    category: &'static str,
    icon: &'static str,
    text: &'static str,
) -> MaiDataItem {
    item("builtin", name, category, icon, Asset::Text { text }, None)
}

fn item(
    item_type: &'static str,
    name: &'static str,
    category: &'static str,
    icon: &'static str,
    asset: Asset,
    asset_night: Option<Asset>,
) -> MaiDataItem {
    MaiDataItem {
        item_type,
        name,
        subtitle: None,
        for_teachers: true,
        leading_icon: None,
        category: Some(category),
        icon: Some(Asset::Relative { url: icon }),
        asset: Some(asset),
        accent: false,
        asset_night,
    }
}
