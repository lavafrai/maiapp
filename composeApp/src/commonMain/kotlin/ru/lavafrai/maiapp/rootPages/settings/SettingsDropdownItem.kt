@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.lavafrai.maiapp.fragments.settings.SettingsDropdownLabel

@Composable
fun <T>SettingsDropdownItem(
    title: String,
    items: List<T>,
    itemContent: @Composable RowScope.(T) -> Unit,
    selected: T,
    selectedContent: @Composable RowScope.(T) -> Unit = itemContent,
    onItemSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(text=title, modifier = Modifier, fontSize = 18.sp)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = Modifier
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                horizontalArrangement = Arrangement.End,
            ) {
                SettingsDropdownLabel(
                    selected = { selectedContent(selected) },
                    expanded = expanded,
                    onExpandedChanged = {
                        expanded = it
                    },
                    modifier = Modifier,
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                ) {
                    Column {//(modifier = Modifier.width(IntrinsicSize.Max)) {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        itemContent(item)
                                    }
                                },
                                onClick = {
                                    onItemSelected(item)
                                    expanded = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}