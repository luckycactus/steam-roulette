package ru.luckycactus.steamroulette.presentation.roulette

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_game_card_stack.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.utils.inflate
import ru.luckycactus.steamroulette.presentation.widget.card_stack.CardStackTouchHelperCallback
import javax.inject.Inject
import kotlin.math.absoluteValue

class RouletteAdapter @Inject constructor(
    private val gameCoverLoader: GlideGameCoverLoader
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

    inner class RouletteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        CardStackTouchHelperCallback.ViewHolderSwipeProgressListener {

        fun bind(game: OwnedGame) {
            itemView.gameView.setGame(game, gameCoverLoader)
        }

        override fun onSwipeProgress(progress: Float, threshold: Float) {
            val thresholdedProgress = (progress / threshold).coerceIn(-1f, 1f)
            itemView.overlayHide.alpha = thresholdedProgress.coerceAtMost(0f).absoluteValue
            //itemView.overlayNext.alpha = thresholdedProgress.coerceAtLeast(0f).absoluteValue
        }
    }
}