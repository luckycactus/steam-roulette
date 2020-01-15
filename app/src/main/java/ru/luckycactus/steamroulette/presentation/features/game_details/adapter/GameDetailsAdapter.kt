package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsItemUiModel
import ru.luckycactus.steamroulette.presentation.features.roulette.GlideGameCoverCacheCleaner
import ru.luckycactus.steamroulette.presentation.utils.inflate
import javax.inject.Inject

class GameDetailsAdapter @Inject constructor(
) : RecyclerView.Adapter<GameDetailsViewHolder<*>>() {

    private val items = mutableListOf<GameDetailsItemUiModel>()

    fun setItems(items: List<GameDetailsItemUiModel>?) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameDetailsViewHolder<*> {
        return when (viewType) {
            R.layout.item_game_details_header -> GameHeaderViewHolder(parent.inflate(viewType))
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GameDetailsViewHolder<*>, position: Int) {
        when (holder) {
            is GameHeaderViewHolder -> holder.bind(items[position] as GameDetailsItemUiModel.Header)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GameDetailsItemUiModel.Header -> R.layout.item_game_details_header
        }
    }
}

abstract class GameDetailsViewHolder<T : GameDetailsItemUiModel>(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    abstract fun bind(item: T)
}