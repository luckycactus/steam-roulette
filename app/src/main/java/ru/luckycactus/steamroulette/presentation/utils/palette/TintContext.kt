package ru.luckycactus.steamroulette.presentation.utils.palette

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.presentation.utils.extensions.compositeOver

class TintContext(
    initialTintColor: Color = Color.Transparent,
    private val coroutineScope: CoroutineScope
) {
    var tintColor by mutableStateOf(initialTintColor)
        private set

    private var tintColorAnimatable = Animatable(tintColor)

    private val currentTintColor: Color
        get() = tintColorAnimatable.value

    @Composable
    fun fabBackground(
        surfaceColor: Color = MaterialTheme.colors.surface,
        tintAlpha: Float = 0.2f
    ): Color {
        return currentTintColor.compositeOver(surfaceColor, tintAlpha)
    }

    @Composable
    fun backgroundGradient(
        staticColor: Color = Color.Transparent,
        tintAlpha: Float = 0.65f
    ): Brush {
        return Brush.verticalGradient(
            listOf(
                currentTintColor.copy(alpha = tintAlpha),
                staticColor
            )
        )
    }

    fun updateColor(color: Color, animate: Boolean = true) {
        if (this.tintColor == color) return

        tintColor = color

        coroutineScope.launch {
            if (animate) {
                tintColorAnimatable.animateTo(tintColor, tween(durationMillis = 300))
            } else {
                tintColorAnimatable.snapTo(color)
            }
        }
    }
}

@Composable
fun rememberTintContext(
    tintColor: Color = Color.Transparent
): TintContext {
    val scope = rememberCoroutineScope()
    return remember {
        TintContext(tintColor, scope)
    }
}