package ru.luckycactus.steamroulette.domain.entity

enum class EnPlayTimeFilter {
    All,
    NotPlayed,
    NotPlayedIn2Weeks;

    companion object {
        private val values = values().associateBy { it.ordinal }

        fun fromOrdinal(ordinal: Int) = values[ordinal]!!
    }
}