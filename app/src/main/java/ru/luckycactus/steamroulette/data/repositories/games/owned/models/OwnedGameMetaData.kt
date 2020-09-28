package ru.luckycactus.steamroulette.data.repositories.games.owned.models

data class OwnedGameMetaData(
    val appId: Int,
    val hidden: Boolean,
    val shown: Boolean
)