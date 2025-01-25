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
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.annotations.LessonAnnotationType
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.utils.LessonSelector

@Composable
fun LessonTypeSelector(
    currentLessonSelectors: List<LessonSelector>,
    allowedLessonTypes: List<LessonType> = listOf(
        LessonType.LABORATORY,
        LessonType.EXAM
    ),
    allowedLessonAnnotations: List<LessonAnnotationType> = listOf(
        LessonAnnotation.HomeWork,
        LessonAnnotation.Colloquium,
        LessonAnnotation.FinalTest,
        LessonAnnotation.ControlWork,
    ),
    onSelectorsChanged: (List<LessonSelector>) -> Unit,
    onDismissRequest: () -> Unit,
    expanded: Boolean,
) {
    var selectedLessonTypes by remember { mutableStateOf(allowedLessonTypes) }
    var selectedLessonAnnotations by remember { mutableStateOf(allowedLessonAnnotations) }
    LaunchedEffect(selectedLessonAnnotations, selectedLessonTypes) {
        val selectors = mutableListOf<LessonSelector>()
        selectedLessonTypes.forEach { lessonType ->
            selectors.add(LessonSelector.type(lessonType))
        }
        selectedLessonAnnotations.forEach { lessonAnnotation ->
            selectors.add(LessonSelector.annotation(lessonAnnotation))
        }

        onSelectorsChanged(selectors)
    }

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
                LessonTypeSelectorItem(
                    title = lessonType.localized(),
                    isSelected = lessonType in selectedLessonTypes,
                    onSelectedChanged = {
                        selectedLessonTypes = if (it) {
                            selectedLessonTypes + lessonType
                        } else {
                            selectedLessonTypes - lessonType
                        }
                    }
                )
            }

            allowedLessonAnnotations.forEach { lessonAnnotation ->
                LessonTypeSelectorItem(
                    title = lessonAnnotation.localized(),
                    isSelected = lessonAnnotation in selectedLessonAnnotations,
                    onSelectedChanged = {
                        selectedLessonAnnotations = if (it) {
                            selectedLessonAnnotations + lessonAnnotation
                        } else {
                            selectedLessonAnnotations - lessonAnnotation
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LessonTypeSelectorItem(
    title: String,
    isSelected: Boolean,
    onSelectedChanged: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Checkbox(
                checked = isSelected,
                onCheckedChange = {
                    onSelectedChanged(it)
                }
            )
        }
    )
}