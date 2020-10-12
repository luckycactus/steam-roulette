package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

class PlaytimeFilterAdapter @Inject constructor() {

    @ToJson
    fun toJson(playtimeFilter: PlaytimeFilter): String {
        return when (playtimeFilter) {
            PlaytimeFilter.All -> "all"
            PlaytimeFilter.NotPlayed -> "not_played"
            is PlaytimeFilter.Limited -> "limited ${playtimeFilter.maxHours}"
        }
    }

    @FromJson
    fun fromJson(json: String): PlaytimeFilter {
        val words = json.split(' ')
        return when (words[0]) {
            "all" -> PlaytimeFilter.All
            "not_played" -> PlaytimeFilter.NotPlayed
            "limited" -> PlaytimeFilter.Limited(words[1].toInt())
            else -> throw IllegalArgumentException()
        }
    }
}