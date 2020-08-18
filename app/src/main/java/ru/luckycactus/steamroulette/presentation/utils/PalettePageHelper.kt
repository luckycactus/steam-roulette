package ru.luckycactus.steamroulette.presentation.utils

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlin.math.abs

class PalettePageHelper(
    private val onColorChange: (Int) -> Unit
) {
    private val colorEvaluator = ArgbEvaluatorCompat()
    private val pageBundles = arrayOf(PageBundle(), PageBundle())
    private var color: Int = 0

    var progress: Float = 0f
        set(value) {
            val needUpdate = field != value
            field = value
            if (needUpdate)
                updateColor()
        }

    fun setPageColor(pageNumber: Int, key: Any?, color: Int) {
        check(pageNumber in 0..1) { "pageNumber must be in 0..1 range" }

        val b = pageBundles[pageNumber]
        if (color == b.targetColor)
            return
        b.targetColor = color
        b.animator?.cancel()
        if (b.key == key && (pageNumber == 0 || progress != 0f)) {
            b.animator = ValueAnimator.ofArgb(b.color, b.targetColor).apply {
                duration = 300
                addUpdateListener {
                    b.color = it.animatedValue as Int
                    updateColor()
                }
                doOnEnd {
                    b.animator = null
                }
            }.also { it.start() }
        } else {
            b.color = b.targetColor
        }
        b.key = key
        updateColor()
    }

    private fun updateColor() {
        val color =
            colorEvaluator.evaluate(abs(progress), pageBundles[0].color, pageBundles[1].color)
        if (this.color != color) {
            onColorChange(color)
            this.color = color
        }
    }

    private class PageBundle {
        var animator: Animator? = null
        var color: Int = 0
        var targetColor = 0
        var key: Any? = null
    }
}