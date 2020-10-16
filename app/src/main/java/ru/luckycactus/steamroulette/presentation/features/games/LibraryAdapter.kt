package ru.luckycactus.steamroulette.presentation.features.games

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_library_game.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.presentation.utils.inflate
import ru.luckycactus.steamroulette.presentation.utils.visibility

class LibraryAdapter(
    private val markHidden: Boolean,
    private val onGameClick: (LibraryGame, List<View>, Boolean) -> Unit
) : PagingDataAdapter<LibraryGame, LibraryAdapter.GameViewHolder>(diffCallback) {

    private val hiddenColorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
        0x90ffffffL.toInt(),
        BlendModeCompat.LIGHTEN
    )

    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder =
        GameViewHolder(parent.inflate(R.layout.item_library_game))

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            item,
            item?.let { tracker?.isSelected(item.header.appId.toLong()) } ?: false
        )
    }

    fun getSelectionKeyForPosition(position: Int): Long? =
        getSelectionKeyForItem(getItem(position))

    private fun getSelectionKeyForItem(item: LibraryGame?): Long? =
        item?.header?.appId?.toLong()

    inner class GameViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private var game: LibraryGame? = null

        init {
            gameView.memoryCacheEnabled = true
            itemView.setOnClickListener {
                game?.let { game ->
                    if (tracker == null || tracker?.selection?.size() == 0) {
                        ViewCompat.setTransitionName(
                            item_library_game,
                            itemView.context.getString(
                                R.string.cardview_shared_element_transition,
                                game.header.appId
                            )
                        )
                        onGameClick(game, listOf(item_library_game), gameView.imageReady)
                    }
                }
            }

        }

        fun bind(game: LibraryGame?, selected: Boolean) {
            this.game = game
            gameView.setGame(game?.header, setTransitionName = false, hd = false)
            gameView.isSelected = selected
            cardViewBottom.isSelected = selected
            checkbox.isSelected = selected
            hidden.visibility(game != null && game.hidden && markHidden)
            gameView.colorFilter =
                if (game != null && game.hidden && markHidden)
                    hiddenColorFilter
                else null
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getSelectionKey(): Long? = getSelectionKeyForItem(game)
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun inSelectionHotspot(e: MotionEvent): Boolean = false
            }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<LibraryGame>() {
            override fun areItemsTheSame(oldItem: LibraryGame, newItem: LibraryGame): Boolean =
                oldItem.header.appId == newItem.header.appId

            override fun areContentsTheSame(oldItem: LibraryGame, newItem: LibraryGame): Boolean =
                oldItem == newItem
        }
    }
}
