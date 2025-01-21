package ru.lavafrai.maiapp.rootPages.maidata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.fragments.data.MaiDataItemCard
import ru.lavafrai.maiapp.models.maidata.MaiDataItem

@Composable
fun MaiDataView(
    data: List<MaiDataItem>,
    modifier: Modifier = Modifier,
) {
    val categories = remember(data) { data.groupBy { it.category }}

    PageColumn(
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    )
                }
            }
        }
    }
}