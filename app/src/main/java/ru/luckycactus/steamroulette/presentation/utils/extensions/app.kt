package ru.luckycactus.steamroulette.presentation.utils.extensions

import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.core.NetworkConnectionException
import ru.luckycactus.steamroulette.data.core.ApiException
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

fun ResourceManager.getCommonErrorDescription(e: Throwable): String {
    return getString(
        when (e) {
            is ApiException -> R.string.error_steam_api_unavailable
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
            R.plurals.playtime_pref_max_time_plurals,
            filter.maxHours,
            filter.maxHours
        )
    }
}

fun ResourceManager.getPlaytimeFilterShortDescription(filter: PlaytimeFilter): String {
    return when (filter) {
        PlaytimeFilter.All -> this.getString(R.string.playtime_pref_all_short)
        PlaytimeFilter.NotPlayed -> this.getString(R.string.playtime_pref_not_played_short)
        is PlaytimeFilter.Limited -> this.getQuantityString(
            R.plurals.playtime_pref_max_time_plurals_short,
            filter.maxHours,
            filter.maxHours
        )
    }
}


