package ru.lavafrai.maiapp.utils

import ru.lavafrai.maiapp.models.maidata.MaiDataItem
import ru.lavafrai.maiapp.models.maidata.asset.Asset

fun MaiDataItem.resolveAsset(night: Boolean): Asset {
    require(asset != null)

    return if (!night) asset!!
    else assetNight ?: asset!!
}

