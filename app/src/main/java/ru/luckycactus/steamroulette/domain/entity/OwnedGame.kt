package ru.luckycactus.steamroulette.domain.entity

data class OwnedGame(
    val appId: Int,
    val name: String,
    val playtime2Weeks: Int,
    val playtimeForever: Int,
    val iconUrl: String,
    val logoUrl: String
) {
    val headerImageUrl
        get() = "https://steamcdn-a.akamaihd.net/steam/apps/$appId/header.jpg"
    val libraryPortraitImageUrl
        get() = "https://steamcdn-a.akamaihd.net/steam/apps/$appId/library_600x900.jpg"
    val libraryPortraitImageUrlHD
        get() = "https://steamcdn-a.akamaihd.net/steam/apps/$appId/library_600x900_2x.jpg"
    val storeUrl
        get() = "https://store.steampowered.com/app/$appId/"
}