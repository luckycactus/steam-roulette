package ru.luckycactus.steamroulette.presentation.ui.compose.extensions

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

fun Color.compositeOver(
    background: Color,
    @FloatRange(from = 0.0, to = 1.0) overlayAlpha: Float = 1f
): Color {
    return copy(alpha = alpha * overlayAlpha).compositeOver(background)
}