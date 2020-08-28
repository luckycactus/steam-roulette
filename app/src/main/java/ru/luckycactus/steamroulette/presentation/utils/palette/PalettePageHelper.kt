package ru.luckycactus.steamroulette.presentation.utils.palette

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlin.math.abs

// can be improved for 3 pages
class PalettePageHelper(
    private val onColorChange: (Int) -> Unit
) {
    private val colorEvaluator = ArgbEvaluatorCompat()
    private val pageBundles = arrayOf(
        PageBundle(),
        PageBundle()
    )
    private var color: Int = 0
    private var progress: Float = 0f

    private val editor by lazy { Editor() }

    @PublishedApi
    internal val `access$editor`: Editor
        get() = editor

    inline fun edit(block: Editor.() -> Unit) {
        `access$editor`.edit(block)
    }

    fun edit() = Editor()

    private fun update(newProgress: Float, newColors: IntArray) {
        if (!checkColorsChanged(newColors)) {
            if (progress != newProgress) {
                progress = newProgress
                updateColor()
            }
            return
        }

        (0..1).forEach { updatePage(it, newProgress, newColors) }
        progress = newProgress
        updateColor()
    }

    private fun updatePage(pageNumber: Int, newProgress: Float, newColors: IntArray) {
        val newColor = newColors[pageNumber]
        val bundle = pageBundles[pageNumber]
        if (newColor == bundle.targetColor)
            return
        if (pageNumber == 0 && newProgress == 0f && progress == 1f) {
            bundle.color = pageBundles[1].color
            bundle.targetColor = bundle.color
        }
        val notAnimate =
            (pageNumber == 0 && newProgress == 0f && this.color == newColor)
                    || (pageNumber == 1 && newProgress == 0f)
        bundle.animator?.cancel()
        bundle.targetColor = newColor
        if (notAnimate) {
            bundle.color = bundle.targetColor
        } else {
            bundle.animator = ValueAnimator.ofArgb(bundle.color, bundle.targetColor).apply {
                duration = 300
                addUpdateListener {
                    bundle.color = it.animatedValue as Int
                    updateColor()
                }
                doOnEnd {
                    bundle.animator = null
                }
            }.also { it.start() }
        }
    }

    private fun checkColorsChanged(newColors: IntArray): Boolean {
        for (i in 0..1) {
            if (pageBundles[i].targetColor != newColors[i])
                return true
        }
        return false
    }

    private fun updateColor() {
        val color =
            colorEvaluator.evaluate(abs(progress), pageBundles[0].color, pageBundles[1].color)
        if (this.color != color) {
            onColorChange(color)
            this.color = color
        }
    }

    inner class Editor {
        var progress: Float = this@PalettePageHelper.progress
            set(value) {
                check(value in 0f..1f)
                field = value
            }

        private val colors = intArrayOf(pageBundles[0].targetColor, pageBundles[1].targetColor)

        fun setPageColor(page: Int, color: Int) {
            colors[page] = color
        }

        inline fun edit(block: Editor.() -> Unit) {
            this.block()
            apply()
        }

        fun apply() {
            _apply()
        }

        private fun _apply() {
            update(progress, colors)
        }
    }

    private class PageBundle {
        var animator: Animator? = null
        var color: Int = 0
        var targetColor = 0
    }
}