package ru.lavafrai.maiapp.fragments

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
    scroll: Boolean = true,
    paddings: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .conditional(scroll) {
                this.verticalScroll(rememberScrollState())
            }
    ) {
        if (paddings) Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.pageColumnPaddings(),
            content = content,
        )
        if (paddings) Spacer(Modifier.height(8.dp))
    }
}