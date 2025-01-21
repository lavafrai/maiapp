package ru.lavafrai.maiapp.rootPages.maidata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.lavafrai.maiapp.models.maidata.MaiDataItem

@Composable
fun MaiDataView(
    data: List<MaiDataItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text(data.toString())
    }
}