package ru.luckycactus.steamroulette.domain

data class OwnedGame(
    val appId: Long,
    val name: String,
    val playtime2Weeks: Int,
    val playTimeForever: Int,
    val iconUrl: String,
    val logoUrl: String
)