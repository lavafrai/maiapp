package ru.lavafrai.maiapp.models.maidata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaiDataItem(
    val type: MaiDataItemType,
    val name: String,
    val forTeachers: Boolean = true,
    val leadingIcon: String? = null,
    val category: String? = null,
    val icon: String? = null,
    val asset: String? = null,
    @SerialName("asset-night") val assetNight: String? = null,
) {
    
}