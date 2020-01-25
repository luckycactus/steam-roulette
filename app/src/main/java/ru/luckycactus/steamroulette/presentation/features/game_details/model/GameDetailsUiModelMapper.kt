package ru.luckycactus.steamroulette.presentation.features.game_details.model

import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.games.entity.GameMinimal
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.Platform
import ru.luckycactus.steamroulette.domain.games.entity.PlatformsAvailability
import javax.inject.Inject

@Reusable
class GameDetailsUiModelMapper @Inject constructor(
    private val resourceManager: ResourceManager
) : Mapper<GameStoreInfo, List<GameDetailsUiModel>>() {

    override fun mapFrom(from: GameStoreInfo): List<GameDetailsUiModel> =
        mutableListOf<GameDetailsUiModel>().apply {
            add(mapHeader(from))
            add(GameDetailsUiModel.Links)
            add(mapShortDescription(from))
            mapPlatforms(from)?.let { add(it) }
            mapLanguages(from)?.let { add(it) }
        }

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

    private fun mapShortDescription(from: GameStoreInfo): GameDetailsUiModel.ShortDescription =
        GameDetailsUiModel.ShortDescription(
            from.shortDescription,//.replace("&quot", "\""), //todo
            from.categories.map { it.description },
            from.genres.map { it.description }
        )

    private fun mapLanguages(from: GameStoreInfo): GameDetailsUiModel.Languages? =
        from.supportedLanguages?.let { GameDetailsUiModel.Languages(it) }

    private fun mapPlatforms(from: GameStoreInfo): GameDetailsUiModel.Platforms? {
        return from.platforms?.let {
            if (it.windows || it.linux || it.mac)
                GameDetailsUiModel.Platforms(it)
            else null
        }
    }

//    private fun mapPlatforms(from: GameStoreInfo): GameDetailsUiModel.SystemReq? {
//        return from.platforms?.let {
//            val platforms = mutableListOf<Pair<String, SystemRequirements?>>()
//            if (it.windows) {
//                platforms.add("Windows" to from.pcRequirements)
//            }
//            if (it.mac) {
//                platforms.add("Mac OS X" to from.macRequirements)
//            }
//            if (it.linux) {
//                platforms.add("SteamOS + Linux" to from.linuxRequirements)
//            }
//            if (platforms.isNotEmpty())
//                GameDetailsUiModel.SystemReq(platforms)
//            else null
//        }
//    }
}