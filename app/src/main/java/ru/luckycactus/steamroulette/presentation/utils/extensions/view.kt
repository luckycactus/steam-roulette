package ru.luckycactus.steamroulette.presentation.utils.extensions

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
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

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)

private val setTransitionAlphaMethod: Method? by lazyNonThreadSafe {
    try {
        View::class.java.getMethod("setTransitionAlpha", Float::class.java)
    } catch (e: NoSuchMethodException) {
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

fun View.addSystemTopPadding(
    targetView: View = this,
    isConsumed: Boolean = false
) {
    doOnApplyWindowInsets { _, insets, initialPadding ->
        targetView.updatePadding(
            top = initialPadding.top + insets.systemWindowInsetTop
        )
        if (isConsumed) {
            insets.inset(0, insets.systemWindowInsetTop, 0, 0)
        } else {
            insets
        }
    }
}

fun View.addSystemBottomPadding(
    targetView: View = this,
    isConsumed: Boolean = false
) {
    doOnApplyWindowInsets { _, insets, initialPadding ->
        targetView.updatePadding(
            bottom = initialPadding.bottom + insets.systemWindowInsetBottom
        )
        if (isConsumed) {
            insets.inset(
                0, 0, 0, insets.systemWindowInsetBottom
            )
        } else {
            insets
        }
    }
}

fun View.doOnApplyWindowInsets(block: (View, insets: WindowInsetsCompat, initialPadding: Rect) -> WindowInsetsCompat) {
    val initialPadding = recordInitialPaddingForView(this)
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        block(v, insets, initialPadding)
    }
    requestApplyInsetsWhenAttached()
}

private fun recordInitialPaddingForView(view: View) =
    Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

fun FloatingActionButton.show(show: Boolean) {
    if (show) show() else hide()
}

inline fun View.changeThroughFade(duration: Long = 200, crossinline block: View.() -> Unit) {
    animate().alpha(0f).setDuration(duration / 2).withEndAction {
        this.block()
        animate().alpha(1f).setDuration(duration / 2)
    }
}