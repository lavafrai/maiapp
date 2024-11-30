@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.navigation.rootPages.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DropdownLabel(
    expanded: Boolean,
    selected: String,
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(
        modifier = modifier
            .clickable { onExpandedChanged(!expanded) }
            .widthIn(min = 128.dp)
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .padding(8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(selected)
        Spacer(modifier = Modifier.width(8.dp))
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
    }
}