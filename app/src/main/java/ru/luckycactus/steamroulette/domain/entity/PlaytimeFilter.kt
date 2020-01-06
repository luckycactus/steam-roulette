package ru.luckycactus.steamroulette.domain.entity

sealed class PlaytimeFilter(
    val type: Type
) {
    object All : PlaytimeFilter(Type.All)
    object NotPlayed : PlaytimeFilter(Type.NotPlayed)
    class Limited(val maxTime: Int) : PlaytimeFilter(Type.Limited)


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