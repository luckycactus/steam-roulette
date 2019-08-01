package ru.luckycactus.steamroulette.presentation.utils

import android.os.Build


fun <T> lazyNonThreadSafe(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun onApiAtLeast(versionCode: Int, post: () -> Unit) {
    if (Build.VERSION.SDK_INT >= versionCode) {
        post()
    }
}