package ru.luckycactus.steamroulette.presentation.features.roulette

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_game_card_stack.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.utils.inflate
import kotlin.math.absoluteValue

class RouletteAdapter @AssistedInject constructor(
    @Assisted private val onGameClick: (View, OwnedGame) -> Unit
) : RecyclerView.Adapter<RouletteAdapter.RouletteViewHolder>() {

    var items: List<OwnedGame>? = null
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
        CardStackTouchHelperCallback.ViewHolderSwipeProgressListener {

        private lateinit var game: OwnedGame

        init {
            gameView.setOnClickListener {
                onGameClick(gameView, game)
            }
        }

        fun bind(game: OwnedGame) {
            this.game = game
            gameView.setGame(game)
        }

        override fun onSwipeProgress(progress: Float, threshold: Float) {
            val thresholdedProgress = (progress / threshold).coerceIn(-1f, 1f)
            overlayHide.alpha = thresholdedProgress.coerceAtMost(0f).absoluteValue
            //itemView.overlayNext.alpha = thresholdedProgress.coerceAtLeast(0f).absoluteValue
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(onGameClick: (View, OwnedGame) -> Unit): RouletteAdapter
    }
}