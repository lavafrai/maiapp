package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation

@Composable
fun AnnotationView(
    annotation: LessonAnnotation,
) {
    Text(annotation.type.name)
}