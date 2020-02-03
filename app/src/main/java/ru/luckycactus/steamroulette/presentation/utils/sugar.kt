package ru.luckycactus.steamroulette.presentation.utils

import android.os.Build


fun <T> lazyNonThreadSafe(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun onApi(versionCode: Int, post: () -> Unit, pre: () -> Unit) {
    if (Build.VERSION.SDK_INT >= versionCode) {
        post()
    } else {
        pre()
    }
}

inline fun onApiAtLeast(versionCode: Int, post: () -> Unit) {
    if (Build.VERSION.SDK_INT >= versionCode) {
        post()
    }
}

inline fun onApiLower(versionCode: Int, pre: () -> Unit) {
    if (Build.VERSION.SDK_INT < versionCode) {
        pre()
    }
}