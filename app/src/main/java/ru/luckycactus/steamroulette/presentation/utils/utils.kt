package ru.luckycactus.steamroulette.presentation.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.lifecycle.LiveData
import kotlin.math.floor
import android.content.pm.PackageManager
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



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


