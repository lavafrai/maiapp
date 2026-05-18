package ru.lavafrai.maiapp.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.rootPages.main.MainNavigationPageId

val MainNavigationPageIdSaver = Saver<MutableState<MainNavigationPageId>, String>(
    restore = { value ->
        mutableStateOf(Json.decodeFromString(value))
    },
    save = { value ->
        Json.encodeToString(value.value)
    }
)