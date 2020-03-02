package ru.luckycactus.steamroulette.presentation.features.game_details.model

import ru.luckycactus.steamroulette.domain.games.entity.*
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState

sealed class GameDetailsUiModel {

    data class Header(
        val gameHeader: GameHeader,
        val developer: String? = null,
        val publisher: String? = null,
        val releaseDate: String? = null
    ) : GameDetailsUiModel()

    data class ShortDescription(
        val value: String?,
        val categories: List<String>?,
        val genres: List<String>?,
        val requiredAge: Int?,
        val metacriticInfo: MetacriticInfo?
    ) : GameDetailsUiModel() {
        fun isEmpty() =
            value.isNullOrEmpty() && categories.isNullOrEmpty() && genres.isNullOrEmpty()
                    && requiredAge == null && metacriticInfo == null
    }

    object Links : GameDetailsUiModel()

    data class Languages(
        val languages: String
    ) : GameDetailsUiModel()

    data class Platforms(
        val platforms: PlatformsAvailability
    ) : GameDetailsUiModel()

    data class Screenshots(
        val screenshots: List<Screenshot>
    ) : GameDetailsUiModel()

    data class DataLoading(
        val contentState: ContentState
    ) : GameDetailsUiModel()
}