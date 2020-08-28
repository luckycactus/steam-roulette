package ru.luckycactus.steamroulette.presentation.utils.palette

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.animation.doOnEnd
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.android.material.color.MaterialColors
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import java.util.*

class TintContext(
    context: Context,
    private val animate: Boolean = true,
    tintColor: Int = Color.TRANSPARENT
) {
    val fabBackgroundTint: LiveData<ColorStateList>
        get() = fabBackgroundTintLiveData

    var tintColor: Int = tintColor
        private set

    private val colorSurface = MaterialColors.getColor(
        context, R.attr.colorSurface, "colorSurface should be set on theme"
    )
    private val colorBackground = MaterialColors.getColor(
        context,
        android.R.attr.colorBackground,
        "colorBackground should be set on theme"
    )

    private val colorLiveData = MutableLiveData<Int>(tintColor)
    private val fabBackgroundTintLiveData = colorLiveData.map {
        ColorStateList.valueOf(
            MaterialColors.layer(
                colorSurface,
                it,
                PaletteUtils.DEFAULT_CONTROLS_TINT_ALPHA
            )
        )
    }

    private val gradientDrawables = WeakHashMap<GradientDrawable, GradientInfo>(1, 1f)
    private val animator by lazyNonThreadSafe {
        TintContextAnimator(this)
    }

    fun updateColor(color: Int) {
        if (animate) {
            animator.updateColor(color)
        } else {
            setColor(color)
        }
    }

    private fun setColor(color: Int) {
        tintColor = color
        colorLiveData.value = color
        gradientDrawables.entries.forEach {
            updateGradientDrawable(it.key, it.value, color)
        }
    }

    fun createTintedBackgroundGradientDrawable(
        orientation: GradientDrawable.Orientation,
        tintAlpha: Float = PaletteUtils.DEFAULT_BG_TINT_ALPHA
    ): GradientDrawable = createTintedGradientDrawable(
        orientation,
        colorBackground,
        tintAlpha
    )

    fun createTintedGradientDrawable(
        orientation: GradientDrawable.Orientation,
        staticColor: Int,
        tintAlpha: Float = PaletteUtils.DEFAULT_BG_TINT_ALPHA
    ): GradientDrawable {
        return GradientDrawable().apply {
            this.orientation = orientation
            val colors = intArrayOf(staticColor, staticColor)
            gradientDrawables[this] = GradientInfo(colors, staticColor, tintAlpha).also {
                updateGradientDrawable(this, it, tintColor)
            }
        }
    }

    private fun updateGradientDrawable(
        drawable: GradientDrawable,
        gradientInfo: GradientInfo,
        tintColor: Int
    ) {
        gradientInfo.colors[0] = MaterialColors.layer(
            gradientInfo.staticColor,
            tintColor,
            gradientInfo.tintAlpha
        )
        drawable.colors = gradientInfo.colors
    }

    private class GradientInfo(
        val colors: IntArray,
        val staticColor: Int,
        val tintAlpha: Float
    )

    private class TintContextAnimator(
        private val tintContext: TintContext
    ) {
        private var targetColor = tintContext.tintColor
        private var animator: Animator? = null

        fun updateColor(tintColor: Int) {
            if (targetColor == tintColor)
                return
            targetColor = tintColor
            animator?.cancel()
            animator = ValueAnimator.ofArgb(tintContext.tintColor, targetColor).apply {
                duration = 300
                addUpdateListener {
                    tintContext.setColor(it.animatedValue as Int)
                }
                doOnEnd {
                    animator = null
                }
            }.also {
                it.start()
            }
        }
    }
}