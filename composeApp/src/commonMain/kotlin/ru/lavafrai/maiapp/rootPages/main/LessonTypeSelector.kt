@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.models.schedule.LessonType

@Composable
fun LessonTypeSelector(
    selectedLessonTypes: List<LessonType>,
    allowedLessonTypes: List<LessonType> = listOf(LessonType.LABORATORY, LessonType.EXAM),
    onLessonTypeSelected: (List<LessonType>) -> Unit,
    onDismissRequest: () -> Unit,
    expanded: Boolean,
) {
    var visible by remember { mutableStateOf(expanded) }
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    val close = {
        scope.launch {
            modalBottomSheetState.hide()
        }.invokeOnCompletion {
            visible = false
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            visible = true
        } else {
            close()
        }
    }


    if (visible) ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = modalBottomSheetState,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            allowedLessonTypes.forEach { lessonType ->
                val isSelected = selectedLessonTypes.contains(lessonType)
                ListItem(
                    headlineContent = { Text(lessonType.localized()) },
                    trailingContent = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) {
                                    onLessonTypeSelected(selectedLessonTypes - lessonType)
                                } else {
                                    onLessonTypeSelected(selectedLessonTypes + lessonType)
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}