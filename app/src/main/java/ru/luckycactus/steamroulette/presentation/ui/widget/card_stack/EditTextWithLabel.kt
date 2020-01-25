package ru.luckycactus.steamroulette.presentation.ui.widget.card_stack

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import androidx.annotation.MainThread

class EditTextWithLabel : EditText {

    var label: CharSequence? = null
        @MainThread
        set(value) {
            val text = textWithoutLabel
            field = if (value != null && !value.startsWith(" ")) {
                " $value"
            } else {
                value
            }
            setText(text)
        }

    val textWithoutLabel: CharSequence
        get() {
            return if (label != null && text.endsWith(label!!)) {
                text.subSequence(0, length() - labelLength)
            } else {
                text
            }
        }

    private val labelLength
        get() = label?.length ?: 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        addTextChangedListener(LabelKeeper())
        setText("")
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        //this method is called from superclass constructor, so child class fields are not initialized yet
        if (label == null || !text.endsWith(label!!)) {
            super.onSelectionChanged(selStart, selEnd)
            return
        }
        val maxSel = length() - labelLength
        if (selEnd > maxSel) {
            setSelection(minOf(selStart, maxSel), maxSel)
        } else {
            super.onSelectionChanged(selStart, selEnd)
        }
    }

    override fun onSaveInstanceState(): Parcelable? =
        SavedState(super.onSaveInstanceState()).apply {
            label = this@EditTextWithLabel.label
        }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        label = savedState.label
        super.onRestoreInstanceState(state.superState)
    }

    private inner class LabelKeeper : TextWatcher {

        private var textWthoutLabelEnd = -1
        private var ignore = false
        private var updatedText: CharSequence? = null

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (ignore)
                return
            val label = label
            if (label.isNullOrEmpty())
                return

            if (!s.endsWith(label)) {
                textWthoutLabelEnd = s.length - count + after
                return
            }

            val labelStart = s.length - label.length
            if (start + count > labelStart) {
                textWthoutLabelEnd = if (start <= labelStart) {
                    start + after
                } else {
                    labelStart
                }
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (ignore || textWthoutLabelEnd < 0)
                return
            if (s.endsWith(label!!)) {
                textWthoutLabelEnd = -1
            } else {
                updatedText = s
            }
        }

        @SuppressLint("SetTextI18n")
        override fun afterTextChanged(s: Editable?) {
            if (ignore || updatedText == null)
                return
            ignore = true
            setText(updatedText!!.substring(0, textWthoutLabelEnd) + label)
            ignore = false
            updatedText = null
            textWthoutLabelEnd = -1
        }

    }

    private class SavedState : BaseSavedState {

        var label: CharSequence? = null

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel?) : super(source) {
            label = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            TextUtils.writeToParcel(label, out, 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}