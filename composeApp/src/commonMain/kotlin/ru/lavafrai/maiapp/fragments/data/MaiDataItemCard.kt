package ru.lavafrai.maiapp.fragments.data

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import compose.icons.FeatherIcons
import compose.icons.feathericons.Menu
import io.ktor.utils.io.core.*
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.fragments.media.SvgIcon
import ru.lavafrai.maiapp.models.maidata.MaiDataItem

@Composable
fun MaiDataItemCard(
    item: MaiDataItem,
    modifier: Modifier = Modifier,
) = AppCard(modifier = modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        if (item.icon != null) {
            SvgIcon(item.icon!!, contentDescription = item.name, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = item.name, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp))
    }
}
