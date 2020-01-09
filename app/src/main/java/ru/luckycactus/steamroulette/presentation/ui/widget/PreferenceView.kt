package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.view_preference.view.*
import ru.luckycactus.steamroulette.R

class PreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context,
    attrs,
    defStyleAttr
) {

    var title: CharSequence?
        set(value) {
            tvPrefTitle.text = value
        }
        get() = tvPrefTitle.text

    var value: CharSequence?
        set(value) {
            tvPrefValue.text = value
        }
        get() = tvPrefValue.text


    init {
        View.inflate(context, R.layout.view_preference, this)
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL

        context.withStyledAttributes(attrs, R.styleable.PreferenceView) {
            title = getString(R.styleable.PreferenceView_prefTitle)
            value = getString(R.styleable.PreferenceView_prefValue)
        }
    }
}