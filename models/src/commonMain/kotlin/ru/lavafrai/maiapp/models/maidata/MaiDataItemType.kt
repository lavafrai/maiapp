package ru.lavafrai.maiapp.models.maidata

import kotlinx.serialization.SerialName

enum class MaiDataItemType {
    @SerialName("builtin") Builtin,
    // @SerialName("webpage") WebPage,
    @SerialName("map") Map,
    // @SerialName("cards") Cards,
    // @SerialName("markup") Markup,
    @SerialName("web") Web,
    @SerialName("nop") DoNothing,
}