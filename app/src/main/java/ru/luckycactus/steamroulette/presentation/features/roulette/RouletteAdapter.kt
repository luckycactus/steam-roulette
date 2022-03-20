package ru.luckycactus.steamroulette.presentation.features.roulette

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.isActive
import ru.luckycactus.steamroulette.databinding.ItemGameCardStackBinding
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCardImageType
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.utils.extensions.layoutInflater
import ru.luckycactus.steamroulette.presentation.utils.palette.PaletteUtils
import kotlin.math.absoluteValue

class RouletteAdapter constructor(
    private val viewLifecycle: Lifecycle,
    private val onGameClick: (GameHeader) -> Unit,
    private val paletteChangeListener: (Int) -> Unit
) : RecyclerView.Adapter<RouletteAdapter.RouletteViewHolder>() {

    var items: List<GameHeader>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouletteViewHolder =
        RouletteViewHolder(
            ItemGameCardStackBinding.inflate(parent.layoutInflater, parent, false)
        )

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(holder: RouletteViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    inner class RouletteViewHolder(
        val binding: ItemGameCardStackBinding
    ) : RecyclerView.ViewHolder(binding.root),
        CardStackTouchHelperCallback.ViewHolderSwipeProgressListener,
        CardStackTouchHelperCallback.ViewHolderVisibleHintListener {

        lateinit var game: GameHeader
        var palette: Palette? = null
            private set

        private fun generatePaletteAndCallListenerAsynchronously(it: Bitmap?) {
            val game = this@RouletteViewHolder.game
            viewLifecycle.coroutineScope.launchWhenCreated {
                val palette = PaletteUtils.generateGameCoverPalette(it)
                if (game == this@RouletteViewHolder.game) {
                    this@RouletteViewHolder.palette = palette
                    if (isActive)
                        paletteChangeListener(bindingAdapterPosition)
                }
            }
        }

        init {
            itemView.setOnClickListener {
                onGameClick(game)
            }
            binding.gameView.onBitmapReady = {
                generatePaletteAndCallListenerAsynchronously(it)
            }
            binding.gameView.defaultTextSize = 36.sp
            binding.gameView.imageType = GameCardImageType.HD
        }

        fun bind(game: GameHeader) {
            if (!this::game.isInitialized || this.game != game)
                this.palette = null
            this.game = game
            setVisibleHint(bindingAdapterPosition == 0)
            binding.gameView.game = game
        }

        override fun onSwipeProgress(progress: Float, threshold: Float) {
            val thresholdedProgress = (progress / threshold).coerceIn(-1f, 1f)
            binding.overlayHide.alpha = thresholdedProgress.coerceAtMost(0f).absoluteValue
            //itemView.overlayNext.alpha = thresholdedProgress.coerceAtLeast(0f).absoluteValue
        }

        // todo compose
        override fun setVisibleHint(visible: Boolean) {
//            binding.gameView.setUserVisibleHint(visible)
        }
    }
}