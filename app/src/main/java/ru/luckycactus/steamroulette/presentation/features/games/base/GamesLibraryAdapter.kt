package ru.luckycactus.steamroulette.presentation.features.games.base

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_library_game.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.utils.inflate

class GamesLibraryAdapter(
    private val onGameClick: (GameHeader, List<View>, Boolean) -> Unit
) : PagingDataAdapter<GameHeader, GamesLibraryAdapter.GameViewHolder>(diffCallback) {
    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder =
        GameViewHolder(parent.inflate(R.layout.item_library_game))

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.bind(item, tracker?.isSelected(item.appId.toLong()) ?: false)
    }

    fun getSelectionKeyForPosition(position: Int): Long? {
        return getItem(position)?.let { getSelectionKeyForItem(it) }
    }

    private fun getSelectionKeyForItem(item: GameHeader): Long {
        return item.appId.toLong()
    }

    inner class GameViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private lateinit var game: GameHeader

        init {
            gameView.memoryCacheEnabled = true
            itemView.setOnClickListener {
                if (tracker == null || tracker?.selection?.size() == 0)
                    onGameClick(game, listOf(gameView), gameView.imageReady)
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
                override fun getSelectionKey(): Long? = getSelectionKeyForItem(game)
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun inSelectionHotspot(e: MotionEvent): Boolean = false
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
