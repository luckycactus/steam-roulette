package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_game_details_links.*
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel

class GameLinksViewHolder(
    itemView: View,
    gameDetailsViewModel: GameDetailsViewModel
) : GameDetailsViewHolder<GameDetailsUiModel.Links>(itemView) {

    init {
        btnStore.setOnClickListener { gameDetailsViewModel.onStoreClick() }
        btnHub.setOnClickListener { gameDetailsViewModel.onHubClick() }
    }

    override fun bind(item: GameDetailsUiModel.Links) {
        //nothing
    }
}