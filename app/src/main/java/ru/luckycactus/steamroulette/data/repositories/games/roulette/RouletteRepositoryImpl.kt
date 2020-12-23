package ru.luckycactus.steamroulette.data.repositories.games.roulette

import android.content.SharedPreferences
import dagger.Reusable
import ru.luckycactus.steamroulette.data.core.edit
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.RouletteRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.presentation.utils.AppUtils.prefKey
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RouletteRepositoryImpl @Inject constructor(
    @Named("roulette") private val prefs: SharedPreferences,
    private val userSession: UserSession
) : RouletteRepository {

    private val editor = prefs.edit()

    override suspend fun getLastTopGameId(): Int? {
        return prefs.getInt(prefKey("last_game", userSession.requireCurrentUser()), -1)
            .let {
                if (it > 0) it else null
            }
    }

    override suspend fun setLastTopGameId(appId: Int?) {
        editor.edit {
            putInt(prefKey("last_game", userSession.requireCurrentUser()), appId ?: -1)
        }
    }

    override suspend fun clearUser(steamId: SteamId) {
        editor.edit {
            remove(prefKey("last_game", steamId))
        }
    }
}