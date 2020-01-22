package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_game_details_short_description.*
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel

class GameShortDescriptionViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsUiModel.ShortDescription>(view) {
    override fun bind(item: GameDetailsUiModel.ShortDescription) {
        tvDescription.text = item.value
    }
}