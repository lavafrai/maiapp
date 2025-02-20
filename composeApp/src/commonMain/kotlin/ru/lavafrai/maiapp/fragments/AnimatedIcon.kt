package ru.lavafrai.maiapp.fragments

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource
import soup.compose.material.motion.MaterialSharedAxisZ

@Composable
fun AnimatedIcon(
    iconPainter: Painter,
    enabledIconPainter: Painter,
    enabled: Boolean,
    contentDescription: String? = null,
) {
    MaterialSharedAxisZ(
        targetState = enabled,
        forward = true,
    ) { state ->
        if (state) Icon(
            painter = enabledIconPainter,
            contentDescription = contentDescription,
        )
        else Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
        )
    }
}

@Composable
fun AnimatedIcon(
    iconPainter: ImageVector,
    enabledIconPainter: ImageVector,
    enabled: Boolean,
    contentDescription: String? = null,
) {
    MaterialSharedAxisZ(
        targetState = enabled,
        forward = true,
    ) { state ->
        if (state) Icon(
            imageVector = enabledIconPainter,
            contentDescription = contentDescription,
        )
        else Icon(
            imageVector = iconPainter,
            contentDescription = contentDescription,
        )
    }
}
