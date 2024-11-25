package ru.lavafrai.maiapp.fragments

import androidx.compose.runtime.compositionLocalOf
import com.dokar.sonner.ToasterState

data class ToasterProvider(
    val toaster: ToasterState,
)

val LocalToaster = compositionLocalOf { throw IllegalStateException("Default toaster isn't supported") ; null as ToasterProvider }