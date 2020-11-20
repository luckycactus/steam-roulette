package ru.luckycactus.steamroulette.domain.games.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameHeader(
    val appId: Int,
    val name: String
): Parcelable {
    constructor(gameStoreInfo: GameStoreInfo) : this(gameStoreInfo.appId, gameStoreInfo.name)
}