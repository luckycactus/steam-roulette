package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_game_details_platforms.*
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.utils.visibility

class GamePlatformsViewHolder(
    view: View,
    viewModel: GameDetailsViewModel
) : GameDetailsViewHolder<GameDetailsUiModel.Platforms>(view) {
    init {
        header.setOnClickListener { viewModel.onSystemRequirementsClick() }
    }

    override fun bind(item: GameDetailsUiModel.Platforms) {
        ivWindows.visibility(item.platforms.windows)
        ivMacOs.visibility(item.platforms.mac)
        ivSteamOs.visibility(item.platforms.linux)
        ivForward.visibility(item.systemRequirementsAvailable)
        itemView.isClickable = item.systemRequirementsAvailable
    }
}
