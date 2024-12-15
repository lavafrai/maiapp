package ru.lavafrai.maiapp.fragments.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown

@Composable
fun SettingsDropdownLabel(
    selected: @Composable () -> Unit,
    expanded: Boolean,
    onExpandedChanged: (Boolean) -> Unit,
    arrangement: Arrangement.Horizontal = Arrangement.End,
    modifier: Modifier = Modifier,
) {
    val rotationDegrees by animateFloatAsState(if (expanded) -180f else 0f)

    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { onExpandedChanged(!expanded) }
            .padding(4.dp),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        selected()
        Spacer(Modifier.width(8.dp))
        Icon(
            FeatherIcons.ChevronDown,
            if (expanded) "list opened" else "list closed",
            modifier = Modifier.rotate(rotationDegrees),
        )
    }
}