package ru.luckycactus.steamroulette.presentation.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T> argument(
    key: String,
    defValue: T? = null
): ReadOnlyProperty<Fragment, T> =
    ReadOnlyProperty { thisRef, _ ->
        val result = thisRef.arguments?.get(key) ?: defValue
        if (result != null && result !is T) {
            throw ClassCastException("Property $key has different class type")
        }
        result as T
    }

inline fun <reified T> Fragment.getCallbacksOrThrow(): T {
    return getCallbacks() ?: throw ClassCastException("${T::class.java} not implemented")
}

inline fun <reified T> Fragment.getCallbacks(): T? {
    return when {
        parentFragment is T -> parentFragment as T
        activity is T -> activity as T
        else -> null
    }
}

fun Activity.hideKeyboard() {
    currentFocus?.apply {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

val Fragment.isFinishing: Boolean
    get() {
        if (requireActivity().isFinishing) {
            return true
        }

        if (isStateSaved) {
            return false
        }

        if (isRemoving) {
            return true
        }

        var anyParentRemoving = false
        var parent = parentFragment
        while (parent != null && !anyParentRemoving) {
            anyParentRemoving = parent.isRemoving
            parent = parent.parentFragment
        }

        if (anyParentRemoving) {
            return true
        }

        return false
    }

inline fun FragmentManager.showIfNotExist(tag: String, createDialogFragment: () -> DialogFragment) {
    if (findFragmentByTag(tag) == null) {
        createDialogFragment().show(this, tag)
    }
}

inline fun FragmentManager.commitIfNotExist(
    tag: String,
    allowStateLoss: Boolean = false,
    body: FragmentTransaction.() -> Unit
) {
    if (findFragmentByTag(tag) == null) {
        commit(allowStateLoss, body)
    }
}