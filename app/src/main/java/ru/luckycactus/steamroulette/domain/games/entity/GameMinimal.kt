package ru.luckycactus.steamroulette.domain.games.entity

data class GameMinimal(
    val appId: Int,
    val name: String
) {
    constructor(ownedGame: OwnedGame) : this(ownedGame.appId, ownedGame.name)

    constructor(gameStoreInfo: GameStoreInfo) : this(gameStoreInfo.appId, gameStoreInfo.name)
}