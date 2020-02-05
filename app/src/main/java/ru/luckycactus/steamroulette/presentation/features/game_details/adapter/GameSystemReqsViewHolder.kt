package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_game_details_system_requirements.*
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.utils.visibility

class GameSystemReqsViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsUiModel.Platforms>(view) {
    override fun bind(item: GameDetailsUiModel.Platforms) {
        with(item.platformsAvailability) {
            ivWindows.visibility(windows)
            ivMacOs.visibility(mac)
            ivSteamOs.visibility(linux)
        }
    }
}
