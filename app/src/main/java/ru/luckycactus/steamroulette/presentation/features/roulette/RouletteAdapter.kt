package ru.luckycactus.steamroulette.presentation.features.roulette

import android.animation.AnimatorInflater
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.isActive
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RouletteViewHolder(ComposeView(parent.context))

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(holder: RouletteViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    inner class RouletteViewHolder(
        composeView: ComposeView
    ) : RecyclerView.ViewHolder(composeView),
        CardStackTouchHelperCallback.ViewHolderSwipeProgressListener,
        CardStackTouchHelperCallback.ViewHolderVisibleHintListener {

        var game: GameHeader? by mutableStateOf(null)
        var overlayHideAlpha: Float by mutableStateOf(0f)

        var palette: Palette? = null
            private set

        init {
            composeView.apply {
                setContent {
                    SteamRouletteTheme {
                        game?.let {
                            RouletteGameCard(
                                game = it,
                                overlayHideAlpha = overlayHideAlpha,
                                onBitmapReady = ::generatePaletteAndCallListenerAsynchronously,
                            )
                        }
                    }
                }
                stateListAnimator = AnimatorInflater.loadStateListAnimator(
                    itemView.context,
                    R.drawable.card_stack_item_state_list_animator
                )
                setOnClickListener {
                    onGameClick(game!!)
                }
            }
        }

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

        fun bind(game: GameHeader) {
            if (this.game != game)
                this.palette = null
            this.game = game
            setVisibleHint(bindingAdapterPosition == 0)
        }

        override fun onSwipeProgress(progress: Float, threshold: Float) {
            val thresholdedProgress = (progress / threshold).coerceIn(-1f, 1f)
            overlayHideAlpha = thresholdedProgress.coerceAtMost(0f).absoluteValue
        }

        override fun setVisibleHint(visible: Boolean) {
        }
    }
}