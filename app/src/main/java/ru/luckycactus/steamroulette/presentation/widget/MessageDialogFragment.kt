package ru.luckycactus.steamroulette.presentation.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Window
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.utils.argument
import ru.luckycactus.steamroulette.presentation.utils.getCallbacks
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe

class MessageDialogFragment : DialogFragment() {

    private val title: String? by argument(ARG_TITLE)
    private val message: String by argument(ARG_MESSAGE)
    private val positive: String? by argument(ARG_POSITIVE)
    private val negative: String? by argument(ARG_NEGATIVE)
    private val dismissOnClick: Boolean by argument(ARG_DISMISS_ON_CLICK)
    private val _tag: String? by argument(ARG_TAG)
    private val cancelable: Boolean by argument(ARG_CANCELLABLE)

    private val clickListener by lazyNonThreadSafe {
        getCallbacks<Callbacks>()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isCancelable = cancelable
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        with(MaterialAlertDialogBuilder(context!!)) {
            setTitle(title)
            setMessage(message)


            setPositiveButton(positive ?: getString(R.string.ok)) { _, _ ->
                clickListener?.onDialogPositiveClick(this@MessageDialogFragment, _tag)
                if (dismissOnClick)
                    dismiss()
            }

            if (negative != null) {
                setNegativeButton(negative) { _, _ ->
                    clickListener?.onDialogNegativeClick(this@MessageDialogFragment, _tag)
                    if (dismissOnClick)
                        dismiss()
                }
            }

            isCancelable = this@MessageDialogFragment.cancelable

            create()
        }.apply {
            if (title == null)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
        }


    override fun onCancel(dialog: DialogInterface) {
        clickListener?.onDialogCancel(_tag)
    }

    interface Callbacks {
        fun onDialogPositiveClick(dialog: MessageDialogFragment, tag: String?) {}
        fun onDialogNegativeClick(dialog: MessageDialogFragment, tag: String?) {}
        fun onDialogCancel(tag: String?) {}
    }

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        private const val ARG_POSITIVE = "ARG_POSITIVE"
        private const val ARG_NEGATIVE = "ARG_NEGATIVE"
        private const val ARG_TAG = "ARG_TAG"
        private const val ARG_DISMISS_ON_CLICK = "ARG_DISMISS_ON_CLICK"
        private const val ARG_CANCELLABLE = "ARG_CANCELLABLE"

        fun create(
            context: Context,
            title: String? = null,
            @StringRes titleResId: Int = 0,
            message: String? = null,
            @StringRes messageResId: Int = 0,
            positiveText: String? = null,
            @StringRes positiveResId: Int = 0,
            negativeText: String? = null,
            @StringRes negativeResId: Int = 0,
            dismissOnClick: Boolean = true,
            cancelable: Boolean = true,
            tag: String? = null
        ) = MessageDialogFragment().apply {
            arguments = bundleOf(
                ARG_TITLE to getString(context, titleResId, title),
                ARG_MESSAGE to getString(context, messageResId, message),
                ARG_POSITIVE to getString(context, positiveResId, positiveText),
                ARG_NEGATIVE to getString(context, negativeResId, negativeText),
                ARG_TAG to tag,
                ARG_DISMISS_ON_CLICK to dismissOnClick,
                ARG_CANCELLABLE to cancelable
            )
        }

        private fun getString(context: Context, resId: Int, text: String?): String? {
            return when {
                text != null -> text
                resId != 0 -> context.getString(resId)
                else -> null
            }
        }
    }
}