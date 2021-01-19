package ru.luckycactus.steamroulette.presentation.ui.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument
import ru.luckycactus.steamroulette.presentation.utils.extensions.getCallback
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe

class MessageDialogFragment : DialogFragment() {

    private var title: String? by argument()
    private var message: String by argument()
    private var positive: String? by argument()
    private var negative: String? by argument()
    private var neutral: String? by argument()
    private var dismissOnClick: Boolean by argument()
    private var isCancellable: Boolean by argument()

    private val callbacks by lazyNonThreadSafe {
        getCallback<Callbacks>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = isCancellable
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        with(MaterialAlertDialogBuilder(requireContext())) {
            setTitle(title)
            setMessage(message)

            setPositiveButton(positive ?: getString(android.R.string.ok)) { _, _ ->
                callbacks?.onMessageDialogResult(this@MessageDialogFragment, Result.Positive)
                if (dismissOnClick)
                    dismiss()
            }

            if (negative != null) {
                setNegativeButton(negative) { _, _ ->
                    callbacks?.onMessageDialogResult(this@MessageDialogFragment, Result.Negative)
                    if (dismissOnClick)
                        dismiss()
                }
            }

            if (neutral != null) {
                setNeutralButton(neutral) { _, _ ->
                    callbacks?.onMessageDialogResult(this@MessageDialogFragment, Result.Neutral)
                    if (dismissOnClick)
                        dismiss()
                }
            }

            isCancelable = this@MessageDialogFragment.isCancellable

            create()
        }


    override fun onCancel(dialog: DialogInterface) {
        callbacks?.onMessageDialogResult(this@MessageDialogFragment, Result.Cancel)
    }

    interface Callbacks {
        fun onMessageDialogResult(dialog: MessageDialogFragment, result: Result) {}
    }

    enum class Result {
        Positive, Negative, Neutral, Cancel
    }

    companion object {
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
            neutralText: String? = null,
            @StringRes neutralResId: Int = 0,
            dismissOnClick: Boolean = true,
            cancelable: Boolean = true,
        ) = MessageDialogFragment().apply {
            this.title = getString(context, titleResId, title)
            this.message = getString(context, messageResId, message)!!
            this.positive = getString(context, positiveResId, positiveText)
            this.negative = getString(context, negativeResId, negativeText)
            this.neutral = getString(context, neutralResId, neutralText)
            this.dismissOnClick = dismissOnClick
            this.isCancellable = cancelable
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