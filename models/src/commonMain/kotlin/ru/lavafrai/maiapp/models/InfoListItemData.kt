package ru.lavafrai.maiapp.models

import kotlinx.serialization.Serializable

@Serializable
class InfoListItemData (
    val type: String,
    val firstText: String,
    val secondText: String? = null,
    val thirdText: String? = null,
    val contactText: String? = null,
    val contactType: String? = null,
    val contactLink: String? = null,
    val topText: String? = null,
)