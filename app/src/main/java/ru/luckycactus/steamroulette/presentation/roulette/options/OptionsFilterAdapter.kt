package ru.luckycactus.steamroulette.presentation.roulette.options

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_filter.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.utils.inflate

class OptionsFilterAdapter(
    private val clickListener: (FilterUiModel) -> Unit
) : ListAdapter<OptionsFilterAdapter.FilterUiModel, OptionsFilterAdapter.FilterViewHolder>(
    diffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(parent.inflate(R.layout.item_filter))
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FilterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var item: FilterUiModel

        init {
            itemView.setOnClickListener {
                clickListener(item)
            }
        }

        fun bind(item: FilterUiModel) {
            this.item = item
            with(itemView) {
                textView.text = item.value
                textView.setTypeface(
                    null,
                    if (item.checked) Typeface.BOLD else Typeface.NORMAL
                )
                ivCheck.visibility = if (item.checked) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    data class FilterUiModel(
        val value: String,
        val checked: Boolean,
        val tag: Any
    )

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<FilterUiModel>() {
            override fun areItemsTheSame(oldItem: FilterUiModel, newItem: FilterUiModel): Boolean {
                return oldItem.value == newItem.value
            }

            override fun areContentsTheSame(
                oldItem: FilterUiModel,
                newItem: FilterUiModel
            ): Boolean {
                return oldItem.checked == newItem.checked
            }

        }
    }
}