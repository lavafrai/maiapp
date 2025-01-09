package ru.lavafrai.maiapp.fragments.animations

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import soup.compose.material.motion.MaterialSharedAxisZ

@Composable
fun AnimatedCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    MaterialSharedAxisZ(
        targetState = checked,
        forward = true,
    ) { checkedTarget ->
        Checkbox(
            checked = checkedTarget,
            onCheckedChange = onCheckedChange,
        )
    }
}