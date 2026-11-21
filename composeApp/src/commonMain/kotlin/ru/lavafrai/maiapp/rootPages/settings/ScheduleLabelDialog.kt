package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Trash2
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.cancel
import maiapp.composeapp.generated.resources.save
import maiapp.composeapp.generated.resources.schedule_label
import maiapp.composeapp.generated.resources.schedule_label_hint
import maiapp.composeapp.generated.resources.schedule_label_remove
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.schedule.MaxScheduleLabelLength
import ru.lavafrai.maiapp.fragments.schedule.ScheduleLabelColors
import ru.lavafrai.maiapp.fragments.schedule.scheduleLabelColor

/** Ширина содержимого диалога: под неё подгоняются поле, палитра и кнопки */
private val LabelDialogWidth = 280.dp

/**
 * Подпись и цвет расписания. Одна и та же подпись показывается во всех списках расписаний,
 * чтобы расписания можно было отличать, не запоминая номера групп.
 */
@Composable
fun ScheduleLabelDialog(
    scheduleId: String,
    onDismiss: () -> Unit,
) {
    val settings by rememberSettings()
    val existing = settings.scheduleLabels[scheduleId]
    var text by remember(scheduleId) { mutableStateOf(existing?.text ?: "") }
    var color by remember(scheduleId) { mutableStateOf(existing?.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.schedule_label)) },
        text = {
            // Ширина диалога определяется его содержимым, поэтому задаём её здесь и у кнопок:
            // без этого AlertDialog растягивается до своей максимальной ширины и на десктопе выглядит огромным
            Column(modifier = Modifier.width(LabelDialogWidth)) {
                OutlinedTextField(
                    value = text,
                    // Длинную подпись не даём вписать: она ломает строки в списках расписаний
                    onValueChange = { newText -> if (newText.length <= MaxScheduleLabelLength) text = newText },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.schedule_label_hint)) },
                    supportingText = {
                        Text("${text.length}/$MaxScheduleLabelLength")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                // Палитра по 6 цветов в ряд: 24 оттенка не помещаются в одну строку на телефоне
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScheduleLabelColors.chunked(6).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowColors.forEach { hex ->
                                val selected = color == hex
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(scheduleLabelColor(hex)!!)
                                        .then(
                                            if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        )
                                        .clickable { color = hex },
                                )
                            }
                        }
                    }
                }
            }
        },
        // Удаление и отмена — слева, подтверждение — справа, как в гайдлайне Material.
        // Ширина задана явно: fillMaxWidth растянул бы весь диалог до максимальной ширины
        confirmButton = {
            Row(
                modifier = Modifier.width(LabelDialogWidth),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (existing != null) {
                        // Иконка вместо длинного «Убрать подпись»: диалог остаётся узким на телефоне
                        IconButton(
                            onClick = {
                                ApplicationSettings.setScheduleLabel(scheduleId, "", null)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Trash2,
                                contentDescription = stringResource(Res.string.schedule_label_remove),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
                }

                Button(
                    onClick = {
                        ApplicationSettings.setScheduleLabel(scheduleId, text, color)
                        onDismiss()
                    }
                ) { Text(stringResource(Res.string.save)) }
            }
        },
    )
}
