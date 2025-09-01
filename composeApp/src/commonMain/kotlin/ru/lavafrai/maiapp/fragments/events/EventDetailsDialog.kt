package ru.lavafrai.maiapp.fragments.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.delete
import maiapp.composeapp.generated.resources.done
import maiapp.composeapp.generated.resources.mark_as
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.repositories.EventRepository
import ru.lavafrai.maiapp.data.repositories.LessonAnnotationsRepository
import ru.lavafrai.maiapp.fragments.DangerButton
import ru.lavafrai.maiapp.fragments.animations.AnimatedCheckBox
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.models.annotations.isAnnotatedBy
import ru.lavafrai.maiapp.models.events.RenderedEvent
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.viewmodels.main.MainPageViewModel

@Composable
fun EventDetailsDialog(
    onNavigateBack: () -> Unit,
    event: RenderedEvent,
    schedule: Schedule,
) {
    val lessonAnnotations by LessonAnnotationsRepository.followLesson(schedule.name, event).collectAsState()

    Dialog(onDismissRequest = onNavigateBack) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    event.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${stringResource(Res.string.mark_as)}…",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                ) {
                    LessonAnnotationsRepository.getMarkableAnnotationTypes().forEach { annotation ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            val isAnnotated = lessonAnnotations.isAnnotatedBy(event, annotation)

                            AnimatedCheckBox(checked = isAnnotated, onCheckedChange = { LessonAnnotationsRepository.toggleAnnotation(
                                schedule.name,
                                event,
                                annotation,
                            ) })

                            Text(
                                annotation.localized(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                val scope = rememberCoroutineScope()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    DangerButton(
                        onClick = {
                            scope.launch {
                                EventRepository.deleteEvent(event.uuid)
                                MainPageViewModel.getInstance().reloadEvents()
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text(stringResource(Res.string.delete) )
                    }
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier,
                    ) {
                        Text(stringResource(Res.string.done))
                    }
                }
            }
        }
    }
}