package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.utils.conditional
import ru.lavafrai.maiapp.utils.pageColumnPaddings

@Composable
fun PageColumn(
    scrollState: ScrollState? = rememberScrollState(),
    paddings: Boolean = true,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .conditional(scrollState != null) {
                this.verticalScroll(scrollState!!)
            }
    ) {
        // if (paddings) Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .pageColumnPaddings(horizontal = paddings),
            content = content,
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
        )
        if (paddings) Spacer(Modifier.height(16.dp))
    }
}