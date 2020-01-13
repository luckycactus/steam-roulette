package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_game_details_header.*
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsItemUiModel
import ru.luckycactus.steamroulette.presentation.features.roulette.GlideGameCoverLoader

class GameHeaderViewHolder(
    view: View,
    private val gameCoverLoader: GlideGameCoverLoader
) : GameDetailsViewHolder<GameDetailsItemUiModel.Header>(view) {

    override fun bind(item: GameDetailsItemUiModel.Header) {
        gameView.setGame(item.game, gameCoverLoader)
    }
}