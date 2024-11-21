@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.lavafrai.maiapp.platform.pointerCursor

@Composable
fun LoginPageButton(
    text: String,
    onClick: () -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()
    val buttonFraction = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1f
        else -> 0.5f
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults
            .buttonColors()
            .copy(containerColor = Color.White.copy(alpha = .1f)),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(buttonFraction)
            .padding(horizontal = 32.dp)
            .pointerCursor(),
    ) {
        Row {
            Text(text = text, color = Color.White, fontSize = 20.sp)
        }
    }
}