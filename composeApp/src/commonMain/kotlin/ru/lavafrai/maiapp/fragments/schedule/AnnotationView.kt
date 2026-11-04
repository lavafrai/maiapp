package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    // Значки встают друг под друга с отступом: раньше они рисовались стопкой с шагом 8dp
    // при размере значка 36dp и почти полностью закрывали друг друга
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        annotations.sortedBy { it.type.priority }.forEach { annotation ->
            AnnotationView(
                annotation = annotation,
                backgroundColor = backgroundColor,
                onOpenAnnotations = onOpenAnnotations,
            )
        }
    }
}

@Composable
fun AnnotationView(
    annotation: LessonAnnotation,
    backgroundColor: Color,
    onOpenAnnotations: () -> Unit = {},
) {
    PairNumber(
        text = annotation.letter(),
        background = annotation.color(),
        color = backgroundColor,
        borderColor = backgroundColor,
        borderWidth = 4.dp,
        onClick = onOpenAnnotations,
    )
}
