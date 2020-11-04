package ru.luckycactus.steamroulette.presentation.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import kotlin.math.floor


fun dpF(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        Resources.getSystem().displayMetrics
    )
}

fun dp(dp: Float): Int {
    return floor(dpF(dp).toDouble()).toInt()
}

fun sp(sp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        Resources.getSystem().displayMetrics
    )
}

private val emptyLiveData by lazyNonThreadSafe { object : LiveData<Any?>() {} }

fun <T> emptyLiveData(): LiveData<T> = emptyLiveData as LiveData<T>

fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun setDrawableColor(drawable: Drawable, @ColorInt color: Int) {
    when (drawable) {
        is ShapeDrawable -> drawable.paint.color = color
        is GradientDrawable -> drawable.setColor(color)
        is ColorDrawable -> drawable.color = color
    }
}

/**
 * @param offset current offset of bottomsheet in range [-1, 1]
 * @param min value of offset when alpha should be 0
 * @param max value of offset when alpha should be 1
 * @return value of alpha for current offset in range [0, 1]
 */
fun bsOffsetToAlpha(offset: Float, min: Float, max: Float): Float {
    return ((offset - min) / (max - min)).coerceIn(0f, 1f)
}

