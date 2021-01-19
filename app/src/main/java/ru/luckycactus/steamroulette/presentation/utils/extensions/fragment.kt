package ru.luckycactus.steamroulette.presentation.utils.extensions

import android.os.Bundle
import androidx.fragment.app.*
import com.google.android.material.snackbar.Snackbar

fun Fragment.ensureArgs() = arguments ?: Bundle().also { arguments = it }

inline fun <reified T> Fragment.requireCallback(): T {
    return getCallback() ?: throw ClassCastException("${T::class.java} not implemented")
}

inline fun <reified T> Fragment.getCallback(): T? {
    return when {
        parentFragment is T -> parentFragment as T
        activity is T -> activity as T
        else -> null
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

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    view?.showSnackbar(message, duration)
}