package ru.luckycactus.steamroulette.data.repositories.games.details.mapper

import dagger.Reusable
import ru.luckycactus.steamroulette.data.repositories.games.details.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.data.repositories.games.details.models.SystemRequirementsEntity
import ru.luckycactus.steamroulette.domain.core.Mapper
import ru.luckycactus.steamroulette.domain.games.entity.*
import javax.inject.Inject

@Reusable
class GameStoreInfoEntityMapper @Inject constructor(
) : Mapper<GameStoreInfoEntity, GameStoreInfo>() {

    private val systemReqsExtraSpacesRegex = "<br>\\s*</li>".toRegex()

    override fun mapFrom(from: GameStoreInfoEntity): GameStoreInfo {
        val platformsAvailability = mapPlatformsAvailability(from)
        return GameStoreInfo(
            from.name,
            from.appId,
            from.requiredAge,
            from.detailedDescription,
            from.aboutTheGame,
            from.shortDescription,
            from.supportedLanguages,
            mapSystemRequirements(from, platformsAvailability),
            from.developers ?: emptyList(),
            from.publishers ?: emptyList(),
            platformsAvailability,
            mapMetacriticInfo(from),
            mapCategories(from),
            mapGenres(from),
            mapScreenshots(from),
            mapReleaseDate(from)
        )
    }

    private fun mapReleaseDate(from: GameStoreInfoEntity): ReleaseDateInfo? {
        return from.releaseDate?.let { ReleaseDateInfo(it.comingSoon, it.date) }
    }

    private fun mapScreenshots(from: GameStoreInfoEntity): List<Screenshot> {
        return from.screenshots?.map { Screenshot(it.id, it.thumbnail, it.full) } ?: emptyList()
    }

    private fun mapGenres(from: GameStoreInfoEntity): List<GameGenre> {
        return from.genres?.map { GameGenre(it.id, it.description) } ?: emptyList()
    }

    private fun mapCategories(from: GameStoreInfoEntity): List<GameCategory> {
        return from.categories?.map { GameCategory(it.id, it.description) } ?: emptyList()
    }

    private fun mapPlatformsAvailability(from: GameStoreInfoEntity): PlatformsAvailability {
        return PlatformsAvailability(
            from.platforms?.windows ?: false,
            from.platforms?.mac ?: false,
            from.platforms?.linux ?: false
        )
    }

    private fun mapSystemRequirements(
        from: GameStoreInfoEntity,
        platforms: PlatformsAvailability
    ): List<SystemRequirements> = listOfNotNull(
        mapPlatformReqs(Platform.Windows, from.pcRequirements, platforms.windows),
        mapPlatformReqs(Platform.Mac, from.macRequirements, platforms.mac),
        mapPlatformReqs(Platform.Linux, from.linuxRequirements, platforms.linux)
    )

    private fun mapPlatformReqs(
        platform: Platform,
        systemRequirements: SystemRequirementsEntity?,
        platformAvailable: Boolean
    ): SystemRequirements? {
        return if (
            systemRequirements != null &&
            (systemRequirements.minimum != null || systemRequirements.recommended != null) &&
            platformAvailable
        ) {
            SystemRequirements(
                platform,
                systemRequirements.minimum?.replace(systemReqsExtraSpacesRegex, "<br></li>"),
                systemRequirements.recommended?.replace(systemReqsExtraSpacesRegex, "<br></li>")
            )
        } else null
    }

    private fun mapMetacriticInfo(from: GameStoreInfoEntity): MetacriticInfo? {
        return from.metacritic?.let { MetacriticInfo(it.score, it.url) }
    }
}