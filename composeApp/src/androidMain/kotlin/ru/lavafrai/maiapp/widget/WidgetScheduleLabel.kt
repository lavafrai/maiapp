package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.platform.getPlatform

@Serializable
data class WidgetScheduleLabel(
    val text: String = "",
    val color: String? = null,
)

@Serializable
private data class SettingsWithLabels(
    val scheduleLabels: Map<String, WidgetScheduleLabel> = emptyMap(),
)

/**
 * Подписи расписаний из настроек приложения.
 *
 * Читаются из общего JSON настроек, а не через модель настроек: так виджет показывает подписи,
 * не завися от того, влито ли уже изменение с подписями в модель.
 */
object WidgetScheduleLabels {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(scheduleIds: Collection<String>): Map<String, WidgetScheduleLabel> {
        if (scheduleIds.isEmpty()) return emptyMap()

        val raw = runCatching { getPlatform().storage().getStringOrNull("settings") }.getOrNull()
            ?: return emptyMap()
        val labels = runCatching { json.decodeFromString<SettingsWithLabels>(raw) }
            .getOrNull()
            ?.scheduleLabels
            ?: return emptyMap()

        return scheduleIds.mapNotNull { id ->
            labels[id]?.takeIf { it.text.isNotBlank() }?.let { id to it }
        }.toMap()
    }
}

/** Цвет подписи из настроек; null, если он не задан или записан неверно */
fun widgetLabelColor(color: String?): Color? {
    val hex = color?.removePrefix("#") ?: return null
    if (hex.length != 6) return null
    val value = hex.toLongOrNull(16) ?: return null
    return Color(0xFF000000L or value)
}
