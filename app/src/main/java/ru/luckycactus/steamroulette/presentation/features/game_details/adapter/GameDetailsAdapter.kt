package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.extensions.LayoutContainer
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.utils.inflate

class GameDetailsAdapter constructor(
    private val waitForHeaderReadyForTransition: Boolean,
    private val onHeaderImageReady: () -> Unit,
    private val gameDetailsViewModel: GameDetailsViewModel
) : ListAdapter<GameDetailsUiModel, GameDetailsViewHolder<*>>(
    diffCallback
) {
    private var headerWasBound = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameDetailsViewHolder<*> {
        val view = parent.inflate(viewType)
        return when (viewType) {
            R.layout.item_game_details_header -> GameHeaderViewHolder(view)
            R.layout.item_game_details_short_description -> GameShortDescriptionViewHolder(view, gameDetailsViewModel)
            R.layout.item_game_details_links -> GameLinksViewHolder(view, gameDetailsViewModel)
            R.layout.item_game_details_languages -> GameLanguagesViewHolder(view)
            R.layout.item_game_details_system_requirements -> GameSystemReqsViewHolder(view)
            R.layout.item_game_details_screenshots -> GameScreenshotsViewHolder(view)
            R.layout.item_empty_layout -> GameLoadingViewHolder(view, gameDetailsViewModel)
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: GameDetailsViewHolder<*>, position: Int) {
        when (holder) {
            is GameHeaderViewHolder -> {
                val shouldWaitForHeader = waitForHeaderReadyForTransition && !headerWasBound
                val listener = if (shouldWaitForHeader)
                    object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            headerWasBound = true
                            onHeaderImageReady()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            headerWasBound = true
                            onHeaderImageReady()
                            return false
                        }

                    }
                else null
                holder.bind(
                    getItem(position) as GameDetailsUiModel.Header,
                    shouldWaitForHeader,
                    listener
                )
                if (!shouldWaitForHeader && !headerWasBound) {
                    onHeaderImageReady()
                    headerWasBound = true
                }
            }
            is GameShortDescriptionViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.ShortDescription)
            is GameLinksViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Links)
            is GameLanguagesViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Languages)
            is GameSystemReqsViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Platforms)
            is GameScreenshotsViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.Screenshots)
            is GameLoadingViewHolder -> holder.bind(getItem(position) as GameDetailsUiModel.DataLoading)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GameDetailsUiModel.Header -> R.layout.item_game_details_header
            is GameDetailsUiModel.ShortDescription -> R.layout.item_game_details_short_description
            is GameDetailsUiModel.Links -> R.layout.item_game_details_links
            is GameDetailsUiModel.Languages -> R.layout.item_game_details_languages
            is GameDetailsUiModel.Platforms -> R.layout.item_game_details_system_requirements
            is GameDetailsUiModel.Screenshots -> R.layout.item_game_details_screenshots
            is GameDetailsUiModel.DataLoading -> R.layout.item_empty_layout
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