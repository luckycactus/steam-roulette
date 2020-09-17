package ru.luckycactus.steamroulette.presentation.features.roulette

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_game_card_stack.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.utils.glide.RequestListenerAdapter
import ru.luckycactus.steamroulette.presentation.utils.inflate
import ru.luckycactus.steamroulette.presentation.utils.palette.PaletteUtils
import kotlin.math.absoluteValue

class RouletteAdapter constructor(
    private val onGameClick: (GameHeader, List<View>, Boolean) -> Unit,
    private val paletteChangeListener: (Int) -> Unit
) : RecyclerView.Adapter<RouletteAdapter.RouletteViewHolder>() {
    var items: List<GameHeader>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouletteViewHolder =
        RouletteViewHolder(parent.inflate(R.layout.item_game_card_stack))

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(holder: RouletteViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    inner class RouletteViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView),
        LayoutContainer,
        CardStackTouchHelperCallback.ViewHolderSwipeProgressListener,
        CardStackTouchHelperCallback.ViewHolderVisibleHintListener {

        lateinit var game: GameHeader
        var palette: Palette? = null
            private set

        private val imageRequestListener = RequestListenerAdapter<Bitmap> {
            if (it == null) {
                paletteChangeListener(bindingAdapterPosition)
            } else {
                val game = this@RouletteViewHolder.game
                PaletteUtils.getGameCoverPalette(it).generate { palette ->
                    if (game == this@RouletteViewHolder.game) {
                        this.palette = palette
                        paletteChangeListener(bindingAdapterPosition)
                    }
                }
            }
        }

        init {
            itemView.setOnClickListener {
                onGameClick(game, listOf(gameView), gameView.imageReady)
            }
        }

        fun bind(game: GameHeader) {
            if (!this::game.isInitialized || this.game != game)
                this.palette = null
            this.game = game
            setVisibleHint(bindingAdapterPosition == 0)
            gameView.setGame(game, false, imageRequestListener)
        }

        override fun onSwipeProgress(progress: Float, threshold: Float) {
            val thresholdedProgress = (progress / threshold).coerceIn(-1f, 1f)
            overlayHide.alpha = thresholdedProgress.coerceAtMost(0f).absoluteValue
            //itemView.overlayNext.alpha = thresholdedProgress.coerceAtLeast(0f).absoluteValue
        }

        override fun setVisibleHint(visible: Boolean) {
            gameView.setUserVisibleHint(visible)
        }
    }
}