package ru.luckycactus.steamroulette.domain.games.entity

data class GameStoreInfo(
    val name: String,
    val appId: Int,
    val requiredAge: Int?,
    val detailedDescription: String,
    val aboutTheGame: String,
    val shortDescription: String,
    val supportedLanguages: String?,
    val requirements: List<SystemRequirements>,
    val developers: List<String>,
    val publishers: List<String>,
    val platforms: PlatformsAvailability,
    val metacritic: MetacriticInfo?,
    val categories: List<GameCategory>,
    val genres: List<GameGenre>,
    val screenshots: List<Screenshot>,
    val releaseDate: ReleaseDateInfo?
)

data class PlatformsAvailability(
    val windows: Boolean,
    val mac: Boolean,
    val linux: Boolean
) {
    val availableOnAnyPlatform = windows || mac || linux
}

data class SystemRequirements(
    val platform: Platform,
    val minimal: String?,
    val recommended: String?
)

data class MetacriticInfo constructor(
    val score: Int,
    val url: String
) {
    val color: Int = when (score) {
        in 75..100 -> 0x81C94E
        in 50..74 -> 0xF8CD55
        else -> 0xEB3323
    }
}

data class GameCategory(
    val id: Int,
    val description: String
)

data class GameGenre(
    val id: Int,
    val description: String
)

data class Screenshot(
    val id: Int,
    val thumbnail: String,
    val full: String
)

data class ReleaseDateInfo(
    val comingSoon: Boolean = false,
    val date: String?
)