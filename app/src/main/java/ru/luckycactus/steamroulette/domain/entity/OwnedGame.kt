package ru.luckycactus.steamroulette.domain.entity

data class OwnedGame(
    val appId: Long,
    val name: String,
    val playtime2Weeks: Int,
    val playtimeForever: Int,
    val iconUrl: String,
    val logoUrl: String
)