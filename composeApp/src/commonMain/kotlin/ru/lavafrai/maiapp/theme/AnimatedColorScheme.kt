package ru.lavafrai.maiapp.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
fun ColorScheme.animated(): ColorScheme {
    val colorScheme = this
    val primary by animateColorAsState(colorScheme.primary)
    val onPrimary by animateColorAsState(colorScheme.onPrimary)
    val invertedPrimary by animateColorAsState(colorScheme.inversePrimary)
    val secondary by animateColorAsState(colorScheme.secondary)
    val onSecondary by animateColorAsState(colorScheme.onSecondary)
    val secondaryContainer by animateColorAsState(colorScheme.secondaryContainer)
    val onSecondaryContainer by animateColorAsState(colorScheme.onSecondaryContainer)
    val tertiary by animateColorAsState(colorScheme.tertiary)
    val onTertiary by animateColorAsState(colorScheme.onTertiary)
    val tertiaryContainer by animateColorAsState(colorScheme.tertiaryContainer)
    val onTertiaryContainer by animateColorAsState(colorScheme.onTertiaryContainer)
    val background by animateColorAsState(colorScheme.background)
    val onBackground by animateColorAsState(colorScheme.onBackground)
    val surface by animateColorAsState(colorScheme.surface)
    val onSurface by animateColorAsState(colorScheme.onSurface)
    val surfaceVariant by animateColorAsState(colorScheme.surfaceVariant)
    val onSurfaceVariant by animateColorAsState(colorScheme.onSurfaceVariant)
    val surfaceTint by animateColorAsState(colorScheme.surfaceTint)
    val inverseSurface by animateColorAsState(colorScheme.inverseSurface)
    val error by animateColorAsState(colorScheme.error)
    val onError by animateColorAsState(colorScheme.onError)
    val errorContainer by animateColorAsState(colorScheme.errorContainer)
    val onErrorContainer by animateColorAsState(colorScheme.onErrorContainer)
    val outline by animateColorAsState(colorScheme.outline)
    val outlineVariant by animateColorAsState(colorScheme.outlineVariant)
    val scrim by animateColorAsState(colorScheme.scrim)
    val surfaceBright by animateColorAsState(colorScheme.surfaceBright)
    val surfaceDim by animateColorAsState(colorScheme.surfaceDim)
    val surfaceContainer by animateColorAsState(colorScheme.surfaceContainer)
    val inverseOnSurface by animateColorAsState(colorScheme.inverseOnSurface)
    val surfaceContainerLowest by animateColorAsState(colorScheme.surfaceContainerLowest)
    val surfaceContainerLow by animateColorAsState(colorScheme.surfaceContainerLow)
    val surfaceContainerHigh by animateColorAsState(colorScheme.surfaceContainerHigh)
    val surfaceContainerHighest by animateColorAsState(colorScheme.surfaceContainerHighest)

    val animatedColorScheme = ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary,
        onPrimaryContainer = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = invertedPrimary,
        surfaceDim = surfaceDim,
        surfaceBright = surfaceBright,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
    )

    return animatedColorScheme
}