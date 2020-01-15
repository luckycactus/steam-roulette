package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_game_details_header.*
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsItemUiModel
import ru.luckycactus.steamroulette.presentation.features.roulette.GlideGameCoverCacheCleaner

class GameHeaderViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsItemUiModel.Header>(view) {

    override fun bind(item: GameDetailsItemUiModel.Header) {
        gameView.setGame(item.game)
    }
}