package ru.luckycactus.steamroulette.presentation.features.game_details.model

import ru.luckycactus.steamroulette.domain.games.entity.*

sealed class GameDetailsUiModel {

    data class Header(
        val gameMinimal: GameMinimal,
        val developer: String? = null,
        val publisher: String? = null,
        val releaseDate: String? = null
    ) : GameDetailsUiModel()

    data class ShortDescription(
        val value: String,
        val categories: List<String>?,
        val genres: List<String>?
    ) : GameDetailsUiModel()

    object Links : GameDetailsUiModel()

    data class Languages(
        val languages: String
    ) : GameDetailsUiModel()

    data class Platforms(
        val platformsAvailability: PlatformsAvailability
    ) : GameDetailsUiModel()

    data class Screenshots(
        val screenshots: List<ScreenshotEntity>
    ): GameDetailsUiModel()
}