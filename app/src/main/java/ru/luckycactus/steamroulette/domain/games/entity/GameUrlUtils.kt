package ru.luckycactus.steamroulette.domain.games.entity

object GameUrlUtils {

    fun headerImage(appId: Int) =
        "https://steamcdn-a.akamaihd.net/steam/apps/$appId/header.jpg"

    fun libraryPortraitImage(appId: Int) =
        "https://steamcdn-a.akamaihd.net/steam/apps/$appId/library_600x900.jpg"

    fun libraryPortraitImageHD(appId: Int) =
        "https://steamcdn-a.akamaihd.net/steam/apps/$appId/library_600x900_2x.jpg"

    fun storePage(appId: Int) =
        "https://store.steampowered.com/app/$appId/"

    fun hubPage(appId: Int) =
        "https://steamcommunity.com/app/$appId"
}