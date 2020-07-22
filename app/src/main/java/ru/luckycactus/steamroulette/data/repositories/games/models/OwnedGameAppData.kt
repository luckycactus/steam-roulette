package ru.luckycactus.steamroulette.data.repositories.games.models

data class OwnedGameAppData(
    val appId: Int,
    val hidden: Boolean,
    val shown: Boolean
)