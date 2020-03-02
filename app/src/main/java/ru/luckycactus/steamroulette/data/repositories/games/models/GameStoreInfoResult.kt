package ru.luckycactus.steamroulette.data.repositories.games.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameStoreInfoResult(
    @Json(name = "data") val gameStoreInfo: GameStoreInfoEntity?,
    @Json(name = "success") val success: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GameStoreInfoEntity(
    @Json(name = "name") val name: String,
    @Json(name = "steam_appid") val appId: Int,
    @Json(name = "required_age") val requiredAge: RequiredAgeEntity?,
    //@Json(name="controller_support") val controllerSupport: String,
    @Json(name = "detailed_description") val detailedDescription: String,
    @Json(name = "about_the_game") val aboutTheGame: String,
    @Json(name = "short_description") val shortDescription: String,
    @Json(name = "supported_languages") val supportedLanguages: String?,
    //@Json(name="reviews") val reviews: String,
    //@Json(name="header_image") val headerImage: String,
    //@Json(name="website") val website: String,
    @Json(name = "pc_requirements") val pcRequirements: SystemRequirementsEntity?,
    @Json(name = "mac_requirements") val macRequirements: SystemRequirementsEntity?,
    @Json(name = "linux_requirements") val linuxRequirements: SystemRequirementsEntity?,
    @Json(name = "developers") val developers: List<String>?,
    @Json(name = "publishers") val publishers: List<String>?,
    @Json(name = "platforms") val platforms: PlatformsAvailabilityEntity?,
    @Json(name = "metacritic") val metacritic: MetacriticInfoEntity?,
    @Json(name = "categories") val categories: List<GameCategoryEntity>?,
    @Json(name = "genres") val genres: List<GameGenreEntity>?,
    @Json(name = "screenshots") val screenshots: List<ScreenshotEntity>?,
    //@Json(name="movies") val trailers: List<TrailerEntity>,
    @Json(name = "release_date") val releaseDate: ReleaseDateInfoEntity?
    //@Json(name="content_descriptors") val contentDescriptors: ContentDescriptorsEntity,
    //@Json(name="background") val background: String
)

data class RequiredAgeEntity(
    val age: Int
)

@JsonClass(generateAdapter = true)
data class SystemRequirementsEntity(
    @Json(name = "minimum") val minimum: String?,
    @Json(name = "recommended") val recommended: String?
)

@JsonClass(generateAdapter = true)
data class PlatformsAvailabilityEntity(
    @Json(name = "windows") val windows: Boolean = false,
    @Json(name = "mac") val mac: Boolean = false,
    @Json(name = "linux") val linux: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MetacriticInfoEntity(
    @Json(name = "score") val score: Int,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class GameCategoryEntity(
    @Json(name = "id") val id: Int,
    @Json(name = "description") val description: String
)

@JsonClass(generateAdapter = true)
data class GameGenreEntity(
    @Json(name = "id") val id: Int,
    @Json(name = "description") val description: String
)

@JsonClass(generateAdapter = true)
data class ScreenshotEntity(
    @Json(name = "id") val id: Int,
    @Json(name = "path_thumbnail") val thumbnail: String,
    @Json(name = "path_full") val full: String
)

@JsonClass(generateAdapter = true)
data class TrailerEntity(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "thumbnail") val thumbnail: String,
    @Json(name = "webm") val webm: WebmEntity,
    @Json(name = "highlight") val highlight: Boolean
)

@JsonClass(generateAdapter = true)
data class WebmEntity(
    @Json(name = "480") val p480: String,
    @Json(name = "max") val max: String
)

@JsonClass(generateAdapter = true)
data class ReleaseDateInfoEntity(
    @Json(name = "coming_soon") val comingSoon: Boolean = false,
    @Json(name = "date") val date: String?
)

@JsonClass(generateAdapter = true)
data class ContentDescriptorsEntity(
    @Json(name = "ids") val ids: List<Int>,
    @Json(name = "notes") val notes: String
)