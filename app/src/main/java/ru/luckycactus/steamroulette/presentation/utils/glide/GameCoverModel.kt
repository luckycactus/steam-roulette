package ru.luckycactus.steamroulette.presentation.utils.glide

import ru.luckycactus.steamroulette.domain.games.entity.GameMinimal
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame

data class GameCoverModel(
    val primary: String,
    val secondary: String
) {
    constructor(appId: Int) : this(
        GameUrlUtils.libraryPortraitImageHD(appId),
        GameUrlUtils.headerImage(appId)
    )

    constructor(ownedGame: OwnedGame) : this(ownedGame.appId)

    constructor(gameStoreInfo: GameStoreInfo) : this(gameStoreInfo.appId)

    constructor(gameMinimal: GameMinimal) : this(gameMinimal.appId)
}