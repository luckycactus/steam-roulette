package ru.luckycactus.steamroulette.domain.games.entity

import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

data class GamesFilter(
    val shown: Boolean? = null,
    val hidden: Boolean? = null,
    val playtime: PlaytimeFilter = PlaytimeFilter.All
) {
    companion object {
        private val empty = GamesFilter()

        fun empty() = empty
    }
}