package ru.luckycactus.steamroulette.presentation.features.hidden_games

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiddenGameViewHolder =
        HiddenGameViewHolder(parent.inflate(R.layout.item_hidden_game))

    override fun onBindViewHolder(holder: HiddenGameViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
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
    lateinit var gameHeader: GameHeader
        private set

    fun bind(gameHeader: GameHeader) {
        gameView.setGame(gameHeader)
        this.gameHeader = gameHeader
    }
}
