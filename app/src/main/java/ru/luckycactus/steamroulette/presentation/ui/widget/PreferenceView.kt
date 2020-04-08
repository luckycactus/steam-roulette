package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
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

    var type: Type = Type.Dropdown
        set(value) {
            val img = when (value) {
                Type.Dropdown -> R.drawable.ic_chevron_down
                Type.Forward -> R.drawable.ic_chevron_right
            }
            imageView.setImageResource(img)
            field = value
        }

    init {
        View.inflate(context, R.layout.view_preference, this)
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL

        context.withStyledAttributes(attrs, R.styleable.PreferenceView) {
            title = getString(R.styleable.PreferenceView_prefTitle)
            value = getString(R.styleable.PreferenceView_prefValue)
            type = Type.fromId(getInt(R.styleable.PreferenceView_type, Type.Dropdown.id))
        }
    }

    override fun setEnabled(enabled: Boolean) {
        tvPrefTitle.isEnabled = enabled
        tvPrefValue.isEnabled = enabled
        imageView.alpha =
            if (enabled)
                1f
            else ResourcesCompat.getFloat(
                resources,
                R.dimen.alpha_emphasis_disabled
            )
        super.setEnabled(enabled)
    }

    enum class Type(
        val id: Int
    ) {
        Dropdown(0),
        Forward(1);

        companion object {
            fun fromId(id: Int): Type {
                for (type in values()) {
                    if (id == type.id)
                        return type
                }
                throw IllegalArgumentException("No PreferenceView.Type with id == $id")
            }
        }
    }
}