package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import ru.luckycactus.steamroulette.databinding.ItemGameDetailsPlatformsBinding
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.utils.extensions.visibility

class GamePlatformsViewHolder(
    private val binding: ItemGameDetailsPlatformsBinding,
    viewModel: GameDetailsViewModel
) : GameDetailsViewHolder<GameDetailsUiModel.Platforms>(binding.root) {

    init {
        binding.header.setOnClickListener { viewModel.onSystemRequirementsClick() }
    }

    override fun bind(item: GameDetailsUiModel.Platforms): Unit = with(binding) {
        ivWindows.visibility(item.platforms.windows)
        ivMacOs.visibility(item.platforms.mac)
        ivSteamOs.visibility(item.platforms.linux)
        ivForward.visibility(item.systemRequirementsAvailable)
        header.isClickable = item.systemRequirementsAvailable
    }
}
