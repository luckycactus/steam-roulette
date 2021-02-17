package ru.luckycactus.steamroulette.data.repositories.games.roulette

import android.content.SharedPreferences
import dagger.Reusable
import ru.luckycactus.steamroulette.data.core.intMultiPref
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.RouletteRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RouletteRepositoryImpl @Inject constructor(
    @Named("roulette") private val prefs: SharedPreferences,
    private val userSession: UserSession
) : RouletteRepository {

    private val currentUser: SteamId
        get() = userSession.requireCurrentUser()

    private val lastGameMultiPref by prefs.intMultiPref("last_game")

    override suspend fun getLastTopGameId(): Int? {
        return lastGameMultiPref[currentUser.as64(), -1]
            .let { if (it > 0) it else null }
    }

    override suspend fun setLastTopGameId(appId: Int?) {
        lastGameMultiPref[currentUser.as64()] = appId ?: -1
    }

    override suspend fun clearUser(steamId: SteamId) {
        lastGameMultiPref.remove(currentUser.as64())
    }
}