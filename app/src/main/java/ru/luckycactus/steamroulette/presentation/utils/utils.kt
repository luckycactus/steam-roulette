package ru.luckycactus.steamroulette.presentation.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Handler
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.R
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

inline fun testPostDelayed(delay: Long, crossinline block: () -> Unit) {
    Handler().postDelayed({ block() }, delay)
}

