package ru.luckycactus.steamroulette.domain.games_filter.entity

sealed class PlaytimeFilter {
    object All : PlaytimeFilter()
    object NotPlayed : PlaytimeFilter()
    data class Limited(val maxHours: Int) : PlaytimeFilter()
}