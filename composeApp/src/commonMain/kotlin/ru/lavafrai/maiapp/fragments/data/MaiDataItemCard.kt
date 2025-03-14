package ru.lavafrai.maiapp.fragments.data

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.lavafrai.maiapp.BuildConfig
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.fragments.hypertext.Hypertext
import ru.lavafrai.maiapp.fragments.media.LoadableSvgIcon
import ru.lavafrai.maiapp.models.maidata.MaiDataItem
import ru.lavafrai.maiapp.models.maidata.asset.AssetLoader
import ru.lavafrai.maiapp.viewmodels.launchCatching

@Composable
fun MaiDataItemCard(
    item: MaiDataItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) = AppCard(
    modifier = modifier,
    onClick = onClick,
    cardBackground = if (item.accent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
    cardContent = if (item.accent) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
) {
    var loadingHash by remember { mutableStateOf(0) }
    var svgIcon by remember { mutableStateOf(Loadable.loading<String>() as Loadable<String>) }
    val assetLoader = remember { AssetLoader.forApi(BuildConfig.API_BASE_URL) }

    LaunchedEffect(item.icon, loadingHash) {
        item.icon ?: return@LaunchedEffect

        launchCatching(onError = { svgIcon = Loadable.error(it as Exception) }) {
            if (svgIcon.data != null) return@launchCatching

            val svgData = item.icon!!.load(assetLoader)
            svgIcon = Loadable.actual(svgData.decodeToString())
        }
    }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.icon != null) {
            LoadableSvgIcon(
                svg = svgIcon,
                modifier = Modifier.size(24.dp),
                reload = { loadingHash++ },
                tint = if (item.accent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column {
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
            )
            if (item.subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Hypertext(item.subtitle!!)
            }
        }
    }

}
