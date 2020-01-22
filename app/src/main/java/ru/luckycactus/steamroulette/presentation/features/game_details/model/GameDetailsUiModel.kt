package ru.luckycactus.steamroulette.presentation.features.game_details.model

import ru.luckycactus.steamroulette.domain.games.entity.GameMinimal

sealed class GameDetailsUiModel {

    data class Header(
        val gameMinimal: GameMinimal
    ) : GameDetailsUiModel()

    data class ShortDescription(
        val value: String
    ): GameDetailsUiModel()
}