package ru.luckycactus.steamroulette.data.repositories.games_filter

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.data.core.intFlow
import ru.luckycactus.steamroulette.data.core.stringFlow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.GamesFilterRepository
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.presentation.utils.AppUtils.prefKey

abstract class GamesFilterRepositoryImpl constructor(
    private val prefs: SharedPreferences,
    moshi: Moshi,
    private val userSession: UserSession
) : GamesFilterRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    private val gamesFilterAdapter = moshi.adapter(GamesFilter::class.java)

    override fun observeMaxHours(default: Int): Flow<Int> {
        return prefs.intFlow(maxHoursKey(currentUser), default)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun observeFilter(default: GamesFilter): Flow<GamesFilter> {
        return prefs.stringFlow(filterKey(currentUser), null)
            .map {
                withContext(Dispatchers.IO) {
                    it?.let {
                        gamesFilterAdapter.fromJson(it)
                    } ?: default
                }
            }
    }

    override suspend fun saveFilter(filter: GamesFilter) {
        val json = gamesFilterAdapter.toJson(filter)
        prefs.edit {
            putString(filterKey(currentUser), json)
            if (filter.playtime is PlaytimeFilter.Limited) {
                putInt(maxHoursKey(currentUser), filter.playtime.maxHours)
            }
        }
    }

    private fun filterKey(steamId: SteamId) = prefKey("filter", "filter", steamId)

    override fun clearUser(steamId: SteamId) {
        prefs.edit {
            remove(filterKey(steamId))
            remove(maxHoursKey(steamId))
        }
    }

    private fun maxHoursKey(steamId: SteamId) = prefKey("filter", "max-hours", steamId)
}