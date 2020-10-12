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
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.GamesFilterRepository
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.entity.UserSession

open class GamesFilterRepositoryImpl constructor(
    private val prefs: SharedPreferences,
    moshi: Moshi,
    private val userSession: UserSession
) : GamesFilterRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    private val gamesFilterAdapter = moshi.adapter(GamesFilter::class.java)

    override fun observeMaxHours(default: Int): Flow<Int> {
        return prefs.intFlow(key(currentUser, "max-hours"), default)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun observeFilter(default: GamesFilter): Flow<GamesFilter> {
        return prefs.stringFlow(key(currentUser, "filter"), null)
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
            putString(key(currentUser, "filter"), json)
            if (filter.playtime is PlaytimeFilter.Limited) {
                putInt(key(currentUser, "max-hours"), filter.playtime.maxHours)
            }
        }
    }

    override fun clearUser(steamId: SteamId) {
        prefs.edit {
            key(currentUser, "filter")
            remove(key(currentUser, "max-hours"))
        }
    }

    private fun key(steamId: SteamId, pref: String) = "filter-${pref}-${steamId.as64()}"
}