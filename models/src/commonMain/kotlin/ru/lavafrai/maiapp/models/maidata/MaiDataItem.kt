package ru.lavafrai.maiapp.models.maidata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.maidata.asset.Asset

@Serializable
data class MaiDataItem(
    val type: MaiDataItemType,
    val name: String,
    val subtitle: String? = null,
    val forTeachers: Boolean = true,
    val leadingIcon: Asset? = null,
    val category: String? = null,
    val icon: Asset? = null,
    val asset: Asset? = null,
    val accent: Boolean = false,
    @SerialName("asset-night") val assetNight: Asset? = null,
) {

}