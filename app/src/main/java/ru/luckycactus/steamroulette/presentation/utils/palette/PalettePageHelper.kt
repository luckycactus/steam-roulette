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
    private val pages = arrayOf(
        Page(0),
        Page(1)
    )
    private var resultColor: Int = 0
    private var progress: Float = 0f

    val editor by lazy { Editor() }

    inline fun edit(block: Editor.() -> Unit) {
        editor.edit(block)
    }

    fun edit() = Editor()

    private fun update(newProgress: Float, newColors: IntArray) {
        val colorsChanged = checkColorsChanged(newColors)
        val progressChanged = progress != newProgress

        if (colorsChanged) {
            pages.forEach {
                updatePage(
                    it,
                    newProgress,
                    newColors[it.number]
                )
            }
        }
        progress = newProgress
        if (colorsChanged || progressChanged)
            updateResultColor()
    }

    private fun checkColorsChanged(newColors: IntArray): Boolean =
        pages.any { it.targetColor != newColors[it.number] }

    private fun updatePage(page: Page, newProgress: Float, newPageColor: Int) {
        if (newPageColor == page.targetColor)
            return

        // item swiped => second page became first, so set first page's color to current color for avoid blinking
        if (page.number == 0 && newProgress == 0f && progress != 0f) {
            page.color = resultColor
        }

        page.animator?.cancel()
        page.targetColor = newPageColor

        val notAnimate =
            (isPageExclusivelyVisible(page, newProgress) && resultColor == newPageColor) ||
                    isPageCompletelyInvisible(page, newProgress)

        if (notAnimate) {
            page.color = page.targetColor
        } else {
            animatePageColor(page)
        }
    }

    private fun isPageExclusivelyVisible(page: Page, progress: Float): Boolean =
        progress == getPageTargetProgress(page)

    private fun isPageCompletelyInvisible(page: Page, progress: Float): Boolean =
        abs(getPageTargetProgress(page) - progress) >= 1f

    private fun getPageTargetProgress(page: Page): Float =
        when (page.number) {
            0 -> 0f
            1 -> 1f
            else -> throw IllegalStateException()
        }

    private fun animatePageColor(page: Page) {
        page.animator = ValueAnimator.ofArgb(page.color, page.targetColor).apply {
            duration = 300
            addUpdateListener {
                page.color = it.animatedValue as Int
                updateResultColor()
            }
            doOnEnd {
                page.animator = null
            }
        }.also {
            it.start()
        }
    }

    private fun updateResultColor() {
        val color = colorEvaluator.evaluate(
            abs(progress),
            pages[0].color,
            pages[1].color
        )
        if (this.resultColor != color) {
            onColorChange(color)
            this.resultColor = color
        }
    }

    inner class Editor {
        var progress: Float = this@PalettePageHelper.progress
            set(value) {
                check(value in 0f..1f)
                field = value
            }

        private val colors = pages.map { it.targetColor }.toIntArray()

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

    private class Page(
        val number: Int
    ) {
        var animator: Animator? = null
        var color: Int = 0
        var targetColor = 0
    }
}