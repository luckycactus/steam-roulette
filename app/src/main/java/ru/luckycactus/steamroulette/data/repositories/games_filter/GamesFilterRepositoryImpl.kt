package ru.luckycactus.steamroulette.data.repositories.games_filter

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.core.intMultiPref
import ru.luckycactus.steamroulette.data.core.typeMultiPref
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.GamesFilterRepository
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.entity.UserSession

abstract class GamesFilterRepositoryImpl constructor(
    prefs: SharedPreferences,
    private val userSession: UserSession
) : GamesFilterRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()


    private val maxHoursMultiPref by prefs.intMultiPref("filter-max-hours")
    private val filterMultiPref by prefs.typeMultiPref<GamesFilter>("filter-filter")

    override fun observeMaxHours(default: Int): Flow<Int> =
        maxHoursMultiPref.flow(currentUser.as64(), default)

    override fun observeFilter(default: GamesFilter): Flow<GamesFilter> =
        filterMultiPref.flow(currentUser.as64(), default)

    override suspend fun saveFilter(filter: GamesFilter) {
        filterMultiPref[currentUser.as64()] = filter
        if (filter.playtime is PlaytimeFilter.Limited) {
            maxHoursMultiPref[currentUser.as64()] = filter.playtime.maxHours
        }
    }

    override fun clearUser(steamId: SteamId) {
        filterMultiPref.remove(steamId.as64())
        maxHoursMultiPref.remove(steamId.as64())
    }
}