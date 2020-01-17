package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.android.extensions.LayoutContainer
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsItemUiModel
import ru.luckycactus.steamroulette.presentation.features.roulette.GlideGameCoverCacheCleaner
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideRequest
import ru.luckycactus.steamroulette.presentation.utils.inflate
import javax.inject.Inject

class GameDetailsAdapter @AssistedInject constructor(
    @Assisted private val enableSharedElementTransition: Boolean,
    @Assisted private val onImageReady: () -> Unit
) : RecyclerView.Adapter<GameDetailsViewHolder<*>>() {
    private val items = mutableListOf<GameDetailsItemUiModel>()
    private var headerWasBound = false

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
            is GameHeaderViewHolder -> {
                val enableTransition = enableSharedElementTransition && !headerWasBound
                val listener = if (enableTransition)
                    object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            headerWasBound = true
                            onImageReady()
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
                            onImageReady()
                            return false
                        }

                    }
                else null
                holder.bind(
                    items[position] as GameDetailsItemUiModel.Header,
                    enableTransition,
                    listener
                    )
                if (!enableTransition && !headerWasBound) {
                    onImageReady()
                    headerWasBound = true
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GameDetailsItemUiModel.Header -> R.layout.item_game_details_header
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(
            enableSharedElementTransition: Boolean,
            onImageReady: () -> Unit
        ): GameDetailsAdapter
    }
}

abstract class GameDetailsViewHolder<T : GameDetailsItemUiModel>(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    abstract fun bind(item: T)
}