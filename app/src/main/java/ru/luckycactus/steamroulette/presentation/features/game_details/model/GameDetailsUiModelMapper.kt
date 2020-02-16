package ru.luckycactus.steamroulette.presentation.features.game_details.model

import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.games.entity.GameMinimal
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GameDetailsUiModelMapper @Inject constructor(
    private val resourceManager: ResourceManager
) : Mapper<GameStoreInfo, List<GameDetailsUiModel>>() {

    override fun mapFrom(from: GameStoreInfo): List<GameDetailsUiModel> =
        listOfNotNull(
            mapHeader(from),
            GameDetailsUiModel.Links,
            mapScrenshots(from),
            mapShortDescription(from),
            mapPlatforms(from),
            mapLanguages(from)
        )

    private fun mapHeader(from: GameStoreInfo): GameDetailsUiModel.Header {
        val releaseDate = from.releaseDate?.let {
            if (!it.date.isNullOrBlank()) {
                it.date
            } else if (it.comingSoon) {
                resourceManager.getString(R.string.coming_soon)
            } else null
        }

        return GameDetailsUiModel.Header(
            GameMinimal(from),
            from.developers?.joinToString(),
            from.publishers?.joinToString(),
            releaseDate
        )
    }

    private fun mapShortDescription(from: GameStoreInfo): GameDetailsUiModel.ShortDescription? {
        val model = GameDetailsUiModel.ShortDescription(
            from.shortDescription,//.replace("&quot", "\""), //todo
            from.categories?.map { it.description },
            from.genres?.map { it.description },
            from.requiredAge?.age,
            from.metacritic
        )
        if (model.isEmpty())
            return null
        return model
    }

    private fun mapLanguages(from: GameStoreInfo): GameDetailsUiModel.Languages? =
        from.supportedLanguages?.let { GameDetailsUiModel.Languages(it) }

    private fun mapPlatforms(from: GameStoreInfo): GameDetailsUiModel.Platforms? {
        return from.platforms?.let {
            if (it.windows || it.linux || it.mac)
                GameDetailsUiModel.Platforms(it)
            else null
        }
    }

    private fun mapScrenshots(from: GameStoreInfo): GameDetailsUiModel.Screenshots? =
        if (from.screenshots?.isNotEmpty() == true)
            GameDetailsUiModel.Screenshots(from.screenshots)
        else null

}