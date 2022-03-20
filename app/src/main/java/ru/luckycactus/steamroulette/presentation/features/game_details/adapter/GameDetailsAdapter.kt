package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.*
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.utils.extensions.layoutInflater

class GameDetailsAdapter constructor(
    private val onHeaderBitmapChanged: (Bitmap?) -> Unit,
    private val gameDetailsViewModel: GameDetailsViewModel
) : ListAdapter<GameDetailsUiModel, GameDetailsViewHolder<*>>(
    diffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameDetailsViewHolder<*> {
        val inflater = parent.layoutInflater
        return when (viewType) {
            R.layout.item_game_details_header -> GameHeaderViewHolder(
                ItemGameDetailsHeaderBinding.inflate(inflater, parent, false),
                onHeaderBitmapChanged
            )
            R.layout.item_game_details_short_description -> GameShortDescriptionViewHolder(
                ItemGameDetailsShortDescriptionBinding.inflate(inflater, parent, false),
                gameDetailsViewModel
            )
            R.layout.item_game_details_links -> GameLinksViewHolder(
                ItemGameDetailsLinksBinding.inflate(inflater, parent, false),
                gameDetailsViewModel
            )
            R.layout.item_game_details_languages -> GameLanguagesViewHolder(
                ItemGameDetailsLanguagesBinding.inflate(inflater, parent, false)
            )
            R.layout.item_game_details_platforms -> GamePlatformsViewHolder(
                ItemGameDetailsPlatformsBinding.inflate(inflater, parent, false),
                gameDetailsViewModel
            )
            R.layout.item_game_details_screenshots -> GameScreenshotsViewHolder(
                ItemGameDetailsScreenshotsBinding.inflate(inflater, parent, false)
            )
            R.layout.item_placeholder -> GamePlaceholderViewHolder(
                ItemPlaceholderBinding.inflate(inflater, parent, false),
                gameDetailsViewModel
            )
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: GameDetailsViewHolder<*>, position: Int) {
        val item = getItem(position)
        when (holder) {
            is GameHeaderViewHolder -> holder.bind(item as GameDetailsUiModel.Header)
            is GameShortDescriptionViewHolder -> holder.bind(item as GameDetailsUiModel.ShortDescription)
            is GameLinksViewHolder -> holder.bind(item as GameDetailsUiModel.Links)
            is GameLanguagesViewHolder -> holder.bind(item as GameDetailsUiModel.Languages)
            is GamePlatformsViewHolder -> holder.bind(item as GameDetailsUiModel.Platforms)
            is GameScreenshotsViewHolder -> holder.bind(item as GameDetailsUiModel.Screenshots)
            is GamePlaceholderViewHolder -> holder.bind(item as GameDetailsUiModel.Placeholder)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GameDetailsUiModel.Header -> R.layout.item_game_details_header
            is GameDetailsUiModel.ShortDescription -> R.layout.item_game_details_short_description
            is GameDetailsUiModel.Links -> R.layout.item_game_details_links
            is GameDetailsUiModel.Languages -> R.layout.item_game_details_languages
            is GameDetailsUiModel.Platforms -> R.layout.item_game_details_platforms
            is GameDetailsUiModel.Screenshots -> R.layout.item_game_details_screenshots
            is GameDetailsUiModel.Placeholder -> R.layout.item_placeholder
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<GameDetailsUiModel>() {
            override fun areItemsTheSame(
                oldItem: GameDetailsUiModel,
                newItem: GameDetailsUiModel
            ): Boolean {
                return oldItem::class == newItem::class
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: GameDetailsUiModel,
                newItem: GameDetailsUiModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

abstract class GameDetailsViewHolder<T : GameDetailsUiModel>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T)
}