package ru.luckycactus.steamroulette.presentation.utils

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.lang.reflect.Method

fun View.visibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
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

fun ViewGroup.inflate(@LayoutRes resId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(resId, this, attachToRoot)

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

fun TextView.setDrawableColor(@ColorInt color: Int) {
    compoundDrawables.filterNotNull().forEach {
        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun TextView.setDrawableColorResource(@ColorRes color: Int) {
    setDrawableColor(getColor(context, color))
}

fun TextView.setDrawableColorFromAttribute(@AttrRes color: Int) {
    setDrawableColor(context.getThemeColorOrThrow(color))
}