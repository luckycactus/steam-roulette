package ru.luckycactus.steamroulette.domain.games.entity

import com.squareup.moshi.JsonClass
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe

@JsonClass(generateAdapter = true)
data class GamesFilter(
    val shown: Boolean? = null,
    val hidden: Boolean? = null,
    val playtime: PlaytimeFilter = PlaytimeFilter.All
) {
    companion object {
        private val empty = GamesFilter()
        private val hidden by lazyNonThreadSafe {
            GamesFilter(hidden = true)
        }

        fun empty() = empty
        fun onlyHidden() = hidden
    }
}