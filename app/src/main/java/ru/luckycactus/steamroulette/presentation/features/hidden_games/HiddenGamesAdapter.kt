package ru.luckycactus.steamroulette.presentation.features.hidden_games

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_hidden_game.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.inflate

class HiddenGamesAdapter : PagedListAdapter<GameHeader, HiddenGameViewHolder>(diffCallback) {
    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiddenGameViewHolder =
        HiddenGameViewHolder(parent.inflate(R.layout.item_hidden_game))

    override fun onBindViewHolder(holder: HiddenGameViewHolder, position: Int) {
        val item = getItem(position)!!
        //todo selection
        holder.bind(item, tracker?.let { it.isSelected(item.appId.toLong()) } ?: false)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)!!.appId.toLong()
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

class HiddenGameViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    init {
        gameView.memoryCacheEnabled = true
    }

    fun bind(gameHeader: GameHeader, selected: Boolean) {
        gameView.setGame(gameHeader)
        gameView.isSelected = selected
        cardViewBottom.isSelected = selected
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
