package ru.luckycactus.steamroulette.presentation.utils.palette

import android.animation.ArgbEvaluator
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TintContextPagerHelper<T : Any>(
    private val tintContext: TintContext,
    private val coroutineScope: CoroutineScope,
    private val size: Int = 2
) {
    private val itemColors = mutableStateMapOf<T, Color>()
    private var items = mutableStateListOf<T>()
    var progress by mutableStateOf(0f)

    private val argbEvaluator = ArgbEvaluator()

    private val pageAnimatables = List(size) { Animatable(Color.Transparent) }

    init {
        require(size > 0)

        coroutineScope.launch {
            snapshotFlow {
                val index = progress.toInt().coerceIn(0, size - 1)
                lerp(
                    pageAnimatables[index].value,
                    pageAnimatables[(index + 1).coerceAtMost(size - 1)].value,
                    progress
                )
            }.collectLatest { color ->
                tintContext.updateColor(color, animate = false)
            }
        }
    }

    fun setItemColor(item: T, color: Color) {
        itemColors[item] = color
        updateColors(animate = true)
    }

    fun getItemColor(item: T): Color? {
        return itemColors[item]
    }

    fun setItems(items: List<T>) {
        require(items.size <= size)
        this.items.clear()
        this.items.addAll(items)
        updateColors(animate = false)
    }

    private fun updateColors(animate: Boolean) {
        pageAnimatables.forEachIndexed { index, page ->
            val item = items.getOrNull(index)
            val color = itemColors[item]
            if (page.targetValue != color) {
                coroutineScope.launch {
                    if (animate && item != null && progress > index - 1 && progress < index + 1) {
                        page.animateTo(
                            color ?: Color.Transparent,
                            animationSpec = tween(200)
                        )
                    } else {
                        page.snapTo(color ?: Color.Transparent)
                    }
                }
            }
        }
    }
}

@Composable
fun <T : Any> rememberPagerTintHelper(
    tintContext: TintContext,
    size: Int = 2
): TintContextPagerHelper<T> {
    val scope = rememberCoroutineScope()
    return remember(tintContext, size) {
        TintContextPagerHelper(tintContext, scope)
    }
}