package ru.luckycactus.steamroulette.presentation.features.hidden_games

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_hidden_game.*
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.utils.inflate

class HiddenGamesAdapter(
    private val onGameClick: (List<View>, GameHeader) -> Unit
) : PagedListAdapter<GameHeader, HiddenGamesAdapter.HiddenGameViewHolder>(diffCallback) {
    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiddenGameViewHolder =
        HiddenGameViewHolder(parent.inflate(R.layout.item_hidden_game))

    override fun onBindViewHolder(holder: HiddenGameViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.bind(item, tracker?.isSelected(item.appId.toLong()) ?: false)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)!!.appId.toLong()
    }

    inner class HiddenGameViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private lateinit var game: GameHeader

        init {
            gameView.memoryCacheEnabled = true
            itemView.setOnClickListener {
                if (tracker?.selection?.size() == 0)
                    onGameClick(gameView.getSharedViews(), game)
            }
        }

        fun bind(gameHeader: GameHeader, selected: Boolean) {
            this.game = gameHeader
            gameView.setGame(gameHeader)
            gameView.isSelected = selected
            cardViewBottom.isSelected = selected
            checkbox.isSelected = selected
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
            return object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getSelectionKey(): Long? {
                    return itemId
                }

                override fun getPosition(): Int {
                    return adapterPosition
                }

                override fun inSelectionHotspot(e: MotionEvent): Boolean {
                    return false
                }
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<GameHeader>() {
            override fun areItemsTheSame(oldItem: GameHeader, newItem: GameHeader): Boolean =
                oldItem.appId == newItem.appId

            override fun areContentsTheSame(oldItem: GameHeader, newItem: GameHeader): Boolean =
                oldItem == newItem

        }
    }
}
