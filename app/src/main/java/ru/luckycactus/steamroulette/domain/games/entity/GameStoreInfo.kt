package ru.luckycactus.steamroulette.domain.games.entity

import com.google.gson.annotations.SerializedName

data class GameStoreInfo(
    @SerializedName("name") val name: String,
    @SerializedName("steam_appid") val appId: Int,
    @SerializedName("required_age") val requiredAge: Int,
    @SerializedName("controller_support") val controllerSupport: String,
    @SerializedName("detailed_description") val detailedDescription: String,
    @SerializedName("about_the_game") val aboutTheGame: String,
    @SerializedName("short_description") val shortDescription: String,
    @SerializedName("supported_languages") val supportedLanguages: String,
    @SerializedName("reviews") val reviews: String,
    @SerializedName("header_image") val headerImage: String,
    @SerializedName("website") val website: String,
    @SerializedName("pc_requirements") val pcRequirements: SystemRequirementsEntity?,
    @SerializedName("mac_requirements") val macRequirements: SystemRequirementsEntity?,
    @SerializedName("linux_requirements") val linuxRequirements: SystemRequirementsEntity?,
    @SerializedName("developers") val developers: List<String>?,
    @SerializedName("publishers") val publishers: List<String>?,
    @SerializedName("platforms") val platforms: PlatformsAvailability,
    @SerializedName("metacritic") val metacritic: MetacriticInfoEntity,
    @SerializedName("categories") val categories: List<GameCategoryEntity>,
    @SerializedName("genres") val genres: List<GameGenreEntity>,
    @SerializedName("screenshots") val screenshots: List<ScreenshotEntity>,
    @SerializedName("movies") val trailers: List<TrailerEntity>,
    @SerializedName("release_date") val releaseDate: ReleaseDateInfoEntity?,
    @SerializedName("content_descriptors") val contentDescriptors: ContentDescriptorsEntity,
    @SerializedName("background") val background: String
)

data class SystemRequirementsEntity(
    @SerializedName("minimum") val minimum: String,
    @SerializedName("recommended") val recommended: String
)

data class PlatformsAvailability(
    @SerializedName("windows") val windows: Boolean,
    @SerializedName("mac") val mac: Boolean,
    @SerializedName("linux") val linux: Boolean
)

data class MetacriticInfoEntity(
    @SerializedName("score") val score: Int,
    @SerializedName("url") val url: String
)

data class GameCategoryEntity(
    @SerializedName("id") val id: Int,
    @SerializedName("description") val description: String
)

data class GameGenreEntity(
    @SerializedName("id") val id: Int,
    @SerializedName("description") val description: String
)

data class ScreenshotEntity(
    @SerializedName("id") val id: Int,
    @SerializedName("path_thumbnail") val thumbnail: String,
    @SerializedName("path_full") val full: String
)

data class TrailerEntity(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("webm") val webm: WebmEntity,
    @SerializedName("highlight") val highlight: Boolean
)

data class WebmEntity(
    @SerializedName("480") val p480: String,
    @SerializedName("max") val max: String
)

data class ReleaseDateInfoEntity(
    @SerializedName("coming_soon") val comingSoon: Boolean,
    @SerializedName("date") val date: String?
)

data class ContentDescriptorsEntity(
    @SerializedName("ids") val ids: List<Int>,
    @SerializedName("notes") val notes: String
)