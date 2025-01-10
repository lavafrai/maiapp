package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.localizers.color
import ru.lavafrai.maiapp.localizers.letter
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation

@Composable
fun AnnotationsView(
    annotations: List<LessonAnnotation>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onOpenAnnotations: () -> Unit = {},
) {
    Box(
        modifier = modifier,
    ) {
        annotations.sortedBy { it.type.priority }.forEachIndexed { index, annotation ->
            AnnotationView(
                annotation,
                topOffset = (annotations.size - index - 1) * 8,
                backgroundColor = backgroundColor,
                onOpenAnnotations = onOpenAnnotations,
            )
        }
    }
}

@Composable
fun AnnotationView(
    annotation: LessonAnnotation,
    topOffset: Int = 0,
    backgroundColor: Color,
    onOpenAnnotations: () -> Unit = {},
) {
    Column {
        Spacer(modifier = Modifier.height(topOffset.dp))

        PairNumber(
            text = annotation.letter(),
            background = annotation.color(),
            color = backgroundColor,
            borderColor = backgroundColor,
            borderWidth = 4.dp,
            onClick = onOpenAnnotations,
        )
    }
}