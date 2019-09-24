package ru.luckycactus.steamroulette.presentation.utils

import android.content.res.Resources
import android.util.TypedValue
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

private val emptyLiveData by lazyNonThreadSafe { object : LiveData<Any?>() {} }

fun <T> emptyLiveData(): LiveData<T> = emptyLiveData as LiveData<T>
