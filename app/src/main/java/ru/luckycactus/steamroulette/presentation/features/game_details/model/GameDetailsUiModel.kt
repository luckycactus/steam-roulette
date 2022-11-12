package ru.luckycactus.steamroulette.presentation.features.game_details.model

import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.MetacriticInfo
import ru.luckycactus.steamroulette.domain.games.entity.PlatformsAvailability
import ru.luckycactus.steamroulette.domain.games.entity.Screenshot

data class GameDetailsUiModel(
    val header: GameHeader,
    val developer: String? = null,
    val publisher: String? = null,
    val releaseDate: String? = null,
    val shortDescription: ShortDescription? = null,
    val languages: String? = null,
    val platforms: Platforms? = null,
    val screenshots: List<Screenshot> = emptyList()
) {
    data class ShortDescription(
        val value: String?,
        val categories: List<String>?,
        val genres: List<String>?,
        val requiredAge: Int?,
        val metacriticInfo: MetacriticInfo?,
        val detailedDescriptionAvailable: Boolean
    ) {
        fun isEmpty() =
            value.isNullOrEmpty() && categories.isNullOrEmpty() && genres.isNullOrEmpty()
                    && requiredAge == null && metacriticInfo == null
    }

    data class Platforms(
        val platforms: PlatformsAvailability,
        val systemRequirementsAvailable: Boolean
    )
}