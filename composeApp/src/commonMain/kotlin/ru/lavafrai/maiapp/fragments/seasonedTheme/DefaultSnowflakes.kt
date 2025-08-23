package ru.lavafrai.maiapp.fragments.seasonedTheme

import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Упрощённые «снежинки» в виде сглаженных многоугольников
// ВНИМАНИЕ: это не заменит internal значения внутри внешней библиотеки,
// если библиотека подключена как зависимость отдельного модуля. Для полной
// замены нужно либо форкнуть библиотеку и заменить у неё этот файл, либо
// добавить в API использование своих Path.

// Генератор сглаженного многоугольника (псевдо‑скругление углов через quadraticBezierTo)
private fun roundedPolygon(
    sides: Int,
    radius: Float,
    centerX: Float = 12f,
    centerY: Float = 12f,
    cornerRadius: Float = 2f
): Path {
    val path = Path()
    if (sides < 3) return path
    val angleStep = (2 * PI / sides).toFloat()
    val pts = Array(sides) { i ->
        val a = -PI.toFloat() / 2f + i * angleStep
        centerX + radius * cos(a) to centerY + radius * sin(a)
    }

    fun lerp(ax: Float, ay: Float, bx: Float, by: Float, t: Float) =
        ax + (bx - ax) * t to ay + (by - ay) * t

    val inset = (cornerRadius / radius).coerceIn(0f, 0.45f)

    fun corner(prev: Pair<Float, Float>, curr: Pair<Float, Float>, next: Pair<Float, Float>) : Triple<Pair<Float,Float>, Pair<Float,Float>, Pair<Float,Float>> {
        val (px, py) = prev
        val (cx, cy) = curr
        val (nx, ny) = next
        val (s1x, s1y) = lerp(cx, cy, px, py, inset)
        val (s2x, s2y) = lerp(cx, cy, nx, ny, inset)
        return Triple(s1x to s1y, cx to cy, s2x to s2y)
    }

    for (i in pts.indices) {
        val prev = pts[(i - 1 + sides) % sides]
        val curr = pts[i]
        val next = pts[(i + 1) % sides]
        val (start, ctrl, end) = corner(prev, curr, next)
        if (i == 0) {
            path.moveTo(start.first, start.second)
        } else {
            path.lineTo(start.first, start.second)
        }
        path.quadraticBezierTo(
            ctrl.first, ctrl.second,
            end.first, end.second
        )
    }
    path.close()
    return path
}

private val SnowflakePentagon by lazy(LazyThreadSafetyMode.NONE) {
    roundedPolygon(
        sides = 5,
        radius = 8f,
        cornerRadius = 2.4f
    )
}

private val SnowflakeHexagon by lazy(LazyThreadSafetyMode.NONE) {
    roundedPolygon(
        sides = 6,
        radius = 8f,
        cornerRadius = 2.2f
    )
}

private val SnowflakeMiniHex by lazy(LazyThreadSafetyMode.NONE) {
    roundedPolygon(
        sides = 6,
        radius = 6f,
        cornerRadius = 2f
    )
}

internal val DefaultSnowflakes by lazy(LazyThreadSafetyMode.NONE) {
    listOf(
        SnowflakePentagon,
        SnowflakeHexagon,
        SnowflakeMiniHex
    )
}

