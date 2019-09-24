package ru.luckycactus.steamroulette.presentation.utils

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.TouchDelegate
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fragment_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.exception.NetworkConnectionException
import ru.luckycactus.steamroulette.domain.exception.ServerException
import ru.luckycactus.steamroulette.presentation.common.Event

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

fun <T> LifecycleOwner.observeNonNull(liveData: LiveData<T?>, body: (T) -> Unit) {
    liveData.observe(this, Observer { it?.let { body(it) } })
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, body: (T) -> Unit) {
    liveData.observe(this, Observer { event -> event.ifNotHandled { body(it) } })
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

fun <T> LiveData<T>.first(block: (T) -> Unit) {
    value?.let {
        block(it)
        return
    }

    observeForever(object : Observer<T> {
        override fun onChanged(t: T) {
            block(t)
            removeObserver(this)
        }

    })
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

//todo add compositeTouchDelegate
fun View.expandTouchArea(desiredWidth: Int, desiredHeight: Int) {
    val parent = parent as View
    parent.post {
        val rect = Rect()
        getHitRect(rect)
        if (desiredWidth >= width) {
            val extraPadding = (desiredWidth - width) / 2
            rect.left -= extraPadding
            rect.right += extraPadding
        }
        if (desiredHeight >= height) {
            val extraPadding = (desiredHeight - height) / 2
            rect.bottom += extraPadding
            rect.top -= extraPadding
        }
        parent.touchDelegate = TouchDelegate(rect, this)
    }
}