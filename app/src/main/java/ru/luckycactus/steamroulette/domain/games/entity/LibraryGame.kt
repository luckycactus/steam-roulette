package ru.luckycactus.steamroulette.domain.games.entity

import androidx.room.Embedded

data class LibraryGame(
    @Embedded val header: GameHeader,
    val hidden: Boolean,
)