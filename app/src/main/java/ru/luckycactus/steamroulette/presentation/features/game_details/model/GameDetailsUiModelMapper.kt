package ru.luckycactus.steamroulette.presentation.features.game_details.model

import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.Mapper
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GameDetailsUiModelMapper @Inject constructor(
    private val resourceManager: ResourceManager
) : Mapper<GameStoreInfo, GameDetailsUiModel>() {

    override fun mapFrom(from: GameStoreInfo): GameDetailsUiModel {
        return GameDetailsUiModel(
            header = GameHeader(from),
            developer = from.developers.joinToString(),
            publisher = from.publishers.joinToString(),
            releaseDate = mapReleaseDate(from),
            shortDescription = mapShortDescription(from),
            languages = from.supportedLanguages,
            platforms = mapPlatforms(from),
            screenshots = from.screenshots
        )
    }

    private fun mapReleaseDate(from: GameStoreInfo): String? {
        return from.releaseDate?.let {
            if (!it.date.isNullOrBlank()) {
                it.date
            } else if (it.comingSoon) {
                resourceManager.getString(R.string.coming_soon)
            } else null
        }
    }

    private fun mapShortDescription(from: GameStoreInfo): GameDetailsUiModel.ShortDescription {
        return GameDetailsUiModel.ShortDescription(
            from.shortDescription,
            from.categories.map { it.description },
            from.genres.map { it.description },
            from.requiredAge,
            from.metacritic,
            from.detailedDescription.isNotBlank()
        )
    }

    private fun mapPlatforms(from: GameStoreInfo): GameDetailsUiModel.Platforms? {
        return if (from.platforms.availableOnAnyPlatform)
            GameDetailsUiModel.Platforms(from.platforms, from.requirements.isNotEmpty())
        else null
    }
}