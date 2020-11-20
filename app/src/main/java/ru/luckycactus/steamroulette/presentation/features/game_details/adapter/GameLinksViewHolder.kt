package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import ru.luckycactus.steamroulette.databinding.ItemGameDetailsLinksBinding
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel

class GameLinksViewHolder(
    binding: ItemGameDetailsLinksBinding,
    gameDetailsViewModel: GameDetailsViewModel
) : GameDetailsViewHolder<GameDetailsUiModel.Links>(binding.root) {

    init {
        binding.btnStore.setOnClickListener { gameDetailsViewModel.onStoreClick() }
        binding.btnHub.setOnClickListener { gameDetailsViewModel.onHubClick() }
    }

    override fun bind(item: GameDetailsUiModel.Links) {
        //nothing
    }
}