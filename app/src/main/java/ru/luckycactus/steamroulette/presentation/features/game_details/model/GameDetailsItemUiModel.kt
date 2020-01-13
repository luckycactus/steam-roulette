package ru.luckycactus.steamroulette.presentation.features.game_details.model

import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame

sealed class GameDetailsItemUiModel {

    data class Header(
        val game: OwnedGame
    ) : GameDetailsItemUiModel()
}