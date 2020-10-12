package ru.luckycactus.steamroulette.presentation.utils

import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.core.NetworkConnectionException
import ru.luckycactus.steamroulette.data.core.ServerException
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

fun ResourceManager.getCommonErrorDescription(e: Exception): String {
    return getString(
        when (e) {
            is ServerException -> R.string.error_steam_api_unavailable
            is NetworkConnectionException -> R.string.error_check_your_connection
            else -> R.string.error_unknown
        }
    )
}

fun ResourceManager.getPlaytimeFilterDescription(filter: PlaytimeFilter): String {
    return when (filter) {
        PlaytimeFilter.All -> this.getString(R.string.playtime_pref_all)
        PlaytimeFilter.NotPlayed -> this.getString(R.string.playtime_pref_not_played)
        is PlaytimeFilter.Limited -> this.getQuantityString(
            R.plurals.playtime_pref_max_time_full_plurals,
            filter.maxHours,
            filter.maxHours
        )
    }
}


