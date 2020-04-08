package ru.luckycactus.steamroulette.presentation.utils

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.lang.reflect.Method

fun View.visibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

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

fun ViewGroup.inflate(@LayoutRes resId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context)
        .inflate(resId, this, attachToRoot)
}

private val setTransitionAlphaMethod: Method? by lazy {
    try {
        View::class.java.getMethod("setTransitionAlpha", Float::class.java)
    } catch (e: NoSuchMethodException) {
        //todo log
        null
    }
}

fun View.trySetTransitionAlpha(alpha: Float, invalidate: Boolean = false): Boolean {
    return setTransitionAlphaMethod?.let {
        it.invoke(this, alpha)
        if (invalidate)
            invalidate()
        true
    } ?: false
}

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    view?.showSnackbar(message, duration)
}

fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    initializer: (Snackbar.() -> Unit)? = null
) {
    with(
        Snackbar.make(
            this,
            message,
            duration
        )
    ) {
        initializer?.invoke(this)
        show()
    }
}

fun ViewGroup.traverseChildren(block: (View) -> Unit) {
    children.forEach {
        block(it)
        if (it is ViewGroup)
            it.traverseChildren(block)
    }
}