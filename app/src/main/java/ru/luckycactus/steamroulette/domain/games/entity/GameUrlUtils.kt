package ru.luckycactus.steamroulette.domain.games.entity

class GameUrlUtils {

    companion object {
        @JvmStatic
        fun headerImage(appId: Int) =
            "https://steamcdn-a.akamaihd.net/steam/apps/$appId/header.jpg"

        @JvmStatic
        fun libraryPortraitImage(appId: Int) =
            "https://steamcdn-a.akamaihd.net/steam/apps/$appId/library_600x900.jpg"

        @JvmStatic
        fun libraryPortraitImageHD(appId: Int) =
            "https://steamcdn-a.akamaihd.net/steam/apps/$appId/library_600x900_2x.jpg"

        @JvmStatic
        fun storePage(appId: Int) =
            "https://store.steampowered.com/app/$appId/"
    }
}