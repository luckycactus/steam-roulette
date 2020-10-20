package ru.luckycactus.steamroulette.domain.games_filter.entity

import com.squareup.moshi.JsonClass
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe

@JsonClass(generateAdapter = true)
data class GamesFilter(
    val shown: Boolean? = null,
    val hidden: Boolean? = null,
    val playtime: PlaytimeFilter = PlaytimeFilter.All
) {
    companion object {
        private val all = GamesFilter()
        private val empty = GamesFilter(hidden = false)
        private val hidden by lazyNonThreadSafe {
            GamesFilter(hidden = true)
        }

        fun withoutHidden() = empty
        fun onlyHidden() = hidden
        fun all() = all
    }
}