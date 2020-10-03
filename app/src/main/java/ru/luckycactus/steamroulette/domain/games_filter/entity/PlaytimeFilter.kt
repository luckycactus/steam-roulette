package ru.luckycactus.steamroulette.domain.games_filter.entity

sealed class PlaytimeFilter(
    val type: Type
) {
    //todo library rename?
    object All : PlaytimeFilter(
        Type.All
    )
    object NotPlayed : PlaytimeFilter(
        Type.NotPlayed
    )
    data class Limited(val maxHours: Int) : PlaytimeFilter(
        Type.Limited
    )


    enum class Type {
        All,
        NotPlayed,
        Limited;

        companion object {
            private val values = values().associateBy { it.ordinal }

            fun fromOrdinal(ordinal: Int) = values.getValue(ordinal)
        }
    }
}