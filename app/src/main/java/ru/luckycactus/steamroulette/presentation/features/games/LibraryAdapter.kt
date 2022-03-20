package ru.luckycactus.steamroulette.presentation.features.games

import android.view.MotionEvent
import android.view.ViewGroup
import androidx.compose.ui.unit.sp
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.luckycactus.steamroulette.databinding.ItemLibraryGameBinding
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCardImageType
import ru.luckycactus.steamroulette.presentation.utils.extensions.layoutInflater

class LibraryAdapter(
    private val onGameClick: (LibraryGame) -> Unit
) : PagingDataAdapter<LibraryGame, LibraryAdapter.GameViewHolder>(diffCallback) {

    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder =
        GameViewHolder(ItemLibraryGameBinding.inflate(parent.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            item,
            item?.let { tracker?.isSelected(item.header.appId.toLong()) } ?: false
        )
    }

    fun getSelectionKeyForPosition(position: Int): Long? =
        getSelectionKeyForItem(getItem(position))

    fun getSelectionKeyForItem(item: LibraryGame?): Long? =
        item?.header?.appId?.toLong()

    inner class GameViewHolder(
        private val binding: ItemLibraryGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        var game: LibraryGame? = null
            private set

        init {
            with(binding) {
                gameView.memoryCacheEnabled = true
                gameView.imageType = GameCardImageType.SD
                gameView.defaultTextSize = 16.sp
                itemView.setOnClickListener {
                    game?.let { game ->
                        if (tracker == null || tracker?.selection?.size() == 0) {
                            onGameClick(game)
                        }
                    }
                }
            }
        }

        fun bind(game: LibraryGame?, selected: Boolean): Unit = with(binding) {
            this@GameViewHolder.game = game
            gameView.game = game?.header
            gameView.isSelected = selected
            cardViewBottom.isSelected = selected
            checkbox.isSelected = selected
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getSelectionKey(): Long? = getSelectionKeyForItem(game)
                override fun getPosition(): Int = bindingAdapterPosition
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
