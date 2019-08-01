package ru.luckycactus.steamroulette.presentation.utils

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.lifecycle.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.exception.NetworkConnectionException
import ru.luckycactus.steamroulette.domain.exception.ServerException

fun View.visibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun <T> MutableLiveData<T>.startWith(item: T): MutableLiveData<T> {
    value = item
    return this
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, body: (T) -> Unit) {
    liveData.observe(this, Observer { body(it) })
}

fun Boolean?.nullIfFalse() = if (this == false) null else this

fun ViewModel.getCommonErrorDescription(resourceManager: ResourceManager, e: Exception): String {
    return resourceManager.getString(
        when (e) {
            is ServerException -> R.string.steam_api_unavailable
            is NetworkConnectionException -> R.string.check_your_connection
            else -> R.string.unknown_error
        }
    )
}

fun Context.getColorFromRes(@ColorRes color: Int): Int {
    onApiAtLeast(Build.VERSION_CODES.M) {
        return resources.getColor(color, theme)
    }
    return resources.getColor(color)
}

fun View.getColorFromRes(@ColorRes color: Int) = context.getColorFromRes(color)


inline fun View.updatePadding(
    @Px left: Int = paddingLeft,
    @Px top: Int = paddingTop,
    @Px right: Int = paddingRight,
    @Px bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}

inline fun View.setPadding(@Px size: Int) {
    setPadding(size, size, size, size)
}