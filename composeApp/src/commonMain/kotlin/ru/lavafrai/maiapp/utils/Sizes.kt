package ru.lavafrai.maiapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*


private fun Int.dpAsSp(density: Density): TextUnit = with(density) {
    this@dpAsSp.dp.toSp()
}

val Int.dpAsSp: TextUnit
    @Composable get() =  this.dpAsSp(density = LocalDensity.current)

private fun Int.spAsDp(density: Density): Dp = with(density) {
    this@spAsDp.sp.toDp()
}

val Int.spAsDp: Dp
    @Composable get() =  this.spAsDp(density = LocalDensity.current)

private fun TextUnit.asDp(density: Density): Dp = with(density) {
    this@asDp.toDp()
}

val TextUnit.asDp: Dp
    @Composable get() =  this.asDp(density = LocalDensity.current)

private fun Dp.asSp(density: Density): TextUnit = with(density) {
    this@asSp.toSp()
}

val Dp.asSp: TextUnit
    @Composable get() =  this.asSp(density = LocalDensity.current)