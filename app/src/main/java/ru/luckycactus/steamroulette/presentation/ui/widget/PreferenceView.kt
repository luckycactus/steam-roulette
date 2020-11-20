package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.ViewPreferenceBinding

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
            binding.tvPrefTitle.text = value
        }
        get() = binding.tvPrefTitle.text

    var value: CharSequence?
        set(value) {
            binding.tvPrefValue.text = value
        }
        get() = binding.tvPrefValue.text

    var type: Type = Type.Dropdown
        set(value) {
            val img = when (value) {
                Type.Dropdown -> R.drawable.ic_chevron_down
                Type.Forward -> R.drawable.ic_chevron_right
            }
            binding.imageView.setImageResource(img)
            field = value
        }

    private val binding =
        ViewPreferenceBinding.inflate(LayoutInflater.from(context), this)

    init {
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL

        context.withStyledAttributes(attrs, R.styleable.PreferenceView) {
            title = getString(R.styleable.PreferenceView_prefTitle)
            value = getString(R.styleable.PreferenceView_prefValue)
            type = Type.fromId(getInt(R.styleable.PreferenceView_type, Type.Dropdown.id))
        }
    }

    override fun setEnabled(enabled: Boolean) {
        binding.tvPrefTitle.isEnabled = enabled
        binding.tvPrefValue.isEnabled = enabled
        binding.imageView.alpha =
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