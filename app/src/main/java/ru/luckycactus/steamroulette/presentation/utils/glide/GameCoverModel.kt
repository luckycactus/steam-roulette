package ru.luckycactus.steamroulette.presentation.utils.glide

import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame

data class GameCoverModel(
    val primary: String,
    val secondary: String
) {
    constructor(game: OwnedGame) : this(
        game.libraryPortraitImageUrlHD,
        game.headerImageUrl
    )
}