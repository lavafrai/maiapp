@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.models.time.DateRange

@Composable
fun WeekSelector(
    onWeekSelected: (DateRange) -> Unit,
    selectedWeek: DateRange,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var visible by remember { mutableStateOf(expanded) }
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            visible = true
        } else {
            scope.launch {
                modalBottomSheetState.hide()
            }.invokeOnCompletion {
                visible = false
            }
        }
    }


    if (visible) ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = modalBottomSheetState,
    ) {
        // Sheet content
    }
}
