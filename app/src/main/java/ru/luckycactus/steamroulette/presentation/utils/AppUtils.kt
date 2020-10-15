package ru.luckycactus.steamroulette.presentation.utils

import ru.luckycactus.steamroulette.domain.common.SteamId

object AppUtils {

    fun prefKey(prefix: String?, pref: String, steamId: SteamId): String =
        prefix?.let {
            "$prefix-$pref-${steamId.as64()}"
        } ?: "$pref-${steamId.as64()}"

    fun prefKey(pref: String, steamId: SteamId): String =
        prefKey(prefix = null, pref = pref, steamId = steamId)
}