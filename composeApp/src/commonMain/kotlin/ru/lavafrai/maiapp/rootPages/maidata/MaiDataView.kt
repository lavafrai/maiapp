package ru.lavafrai.maiapp.rootPages.maidata

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertOctagon
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.BuildConfig
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.fragments.data.MaiDataItemCard
import ru.lavafrai.maiapp.models.maidata.MaiDataManifest

@Composable
fun MaiDataView(
    manifest: MaiDataManifest,
    modifier: Modifier = Modifier,
) = PageColumn(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    // TODO before release maidata should be done or removed!!!
    val categories = remember(manifest) { manifest.data.groupBy { it.category } }
    val appContext = LocalApplicationContext.current

    if (manifest.version > BuildConfig.MAIDATA_SUPPORTED_MANIFEST_VERSION) UnsupportedManifestVersionView(
        supportedVersion = BuildConfig.MAIDATA_SUPPORTED_MANIFEST_VERSION,
        actualVersion = manifest.version,
        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(0.8f),
    )
    else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.height(0.dp))
        categories.forEach { (category, items) ->
            if (category != null) Text(
                text = category,
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge,
            )

            items.forEach { item ->
                MaiDataItemCard(
                    item = item,
                    modifier = Modifier,
                    onClick = { appContext.openMaiDataItem(item) },
                )
            }
        }
    }
}

@Composable
fun UnsupportedManifestVersionView(
    supportedVersion: Int,
    actualVersion: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                FeatherIcons.AlertOctagon,
                contentDescription = "Unsupported manifest version",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(Res.string.unsupported_manifest_version),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.6f)) {
                Column {
                    Text(
                        text = "${stringResource(Res.string.supported_version)}: $supportedVersion",
                    )
                    Text(
                        text = "${stringResource(Res.string.received_version)}: $actualVersion",
                    )
                }
            }
            Text(
                text = stringResource(Res.string.unsupported_manifest_version_description),
                textAlign = TextAlign.Center,
            )
        }
    }
}
