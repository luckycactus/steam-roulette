package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.widget.GameView
import ru.luckycactus.steamroulette.presentation.utils.extensions.inflate

class GameDetailsAdapter constructor(
    // store api can return different id, but we need use old one for working shared element transition
    private val transitionGameId: Int,
    private val waitForImageReadyForTransition: Boolean,
    private val onHeaderImageReady: () -> Unit,
    private val onHeaderBitmapChanged: (Bitmap?) -> Unit,
    private val gameDetailsViewModel: GameDetailsViewModel
) : ListAdapter<GameDetailsUiModel, GameDetailsViewHolder<*>>(
    diffCallback
) {
    private var imageReady = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameDetailsViewHolder<*> {
        val view = parent.inflate(viewType)
        return when (viewType) {
            R.layout.item_game_details_header -> GameHeaderViewHolder(view, transitionGameId)
            R.layout.item_game_details_short_description -> GameShortDescriptionViewHolder(
                view,
                gameDetailsViewModel
            )
            R.layout.item_game_details_links -> GameLinksViewHolder(view, gameDetailsViewModel)
            R.layout.item_game_details_languages -> GameLanguagesViewHolder(view)
            R.layout.item_game_details_platforms -> GamePlatformsViewHolder(
                view,
                gameDetailsViewModel
            )
            R.layout.item_game_details_screenshots -> GameScreenshotsViewHolder(view)
            R.layout.item_placeholder -> GamePlaceholderViewHolder(view, gameDetailsViewModel)
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: GameDetailsViewHolder<*>, position: Int) {
        when (holder) {
            is GameHeaderViewHolder -> {
                val shouldWaitForHeader = waitForImageReadyForTransition && !imageReady

                val listener = GameView.Listener {
                    if (!imageReady) {
                        imageReady = true
                        onHeaderImageReady()
                    }
                    onHeaderBitmapChanged(it)
                }

                holder.bind(
                    getItem(position) as GameDetailsUiModel.Header,
                    shouldWaitForHeader,
                    listener
                )
                if (!shouldWaitForHeader && !imageReady) {
                    onHeaderImageReady()
                    imageReady = true
                }
            }
            is GameShortDescriptionViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.ShortDescription)
            is GameLinksViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Links)
            is GameLanguagesViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Languages)
            is GamePlatformsViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Platforms)
            is GameScreenshotsViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Screenshots)
            is GamePlaceholderViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Placeholder)
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
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    abstract fun bind(item: T)
}