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

//todo
fun longLog(tag: String, message: String) {
    val maxLogSize = 1000
    for (i in 0..message.length / maxLogSize) {
        val start = i * maxLogSize
        var end = (i + 1) * maxLogSize
        end = if (end > message.length) message.length else end
        Log.d(tag, message.substring(start, end))
    }
}

