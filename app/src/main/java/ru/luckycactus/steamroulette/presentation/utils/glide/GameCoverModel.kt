package ru.luckycactus.steamroulette.presentation.utils.glide

import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils

data class GameCoverModel(
    val primary: String,
    val secondary: String
) {
    constructor(appId: Int) : this(
        GameUrlUtils.libraryPortraitImageHD(appId),
        GameUrlUtils.headerImage(appId)
    )

    constructor(gameStoreInfo: GameStoreInfo) : this(gameStoreInfo.appId)

    constructor(gameHeader: GameHeader) : this(gameHeader.appId)
}