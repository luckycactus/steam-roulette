package ru.luckycactus.steamroulette.presentation.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

fun Context.getThemeColorOrThrow(@AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    if (theme.resolveAttribute(resId, typedValue, true)) {
        return typedValue.data
    } else {
        throw IllegalArgumentException("Attribute not set on theme")
    }
}