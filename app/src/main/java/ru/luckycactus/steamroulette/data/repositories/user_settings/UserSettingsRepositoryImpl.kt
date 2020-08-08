package ru.luckycactus.steamroulette.data.repositories.user_settings

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.core.intFlow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Named

@Reusable
class UserSettingsRepositoryImpl @Inject constructor(
    @Named("user-settings") private val userSettingsPrefs: SharedPreferences,
    private val userSession: UserSession
) : UserSettingsRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    override fun observePlaytimeFilterType(default: PlaytimeFilter.Type): Flow<PlaytimeFilter.Type> =
        userSettingsPrefs.intFlow(playTimeKey(currentUser), default.ordinal)
            .map { PlaytimeFilter.Type.fromOrdinal(it) }

    override fun observeMaxPlaytime(default: Int): Flow<Int> =
        userSettingsPrefs.intFlow(maximumPlayTimeKey(currentUser), default)

    override fun savePlayTimeFilterType(filterType: PlaytimeFilter.Type) {
        userSettingsPrefs.edit {
            putInt(playTimeKey(currentUser), filterType.ordinal)
        }
    }

    override fun saveMaxPlaytime(maxHours: Int) {
        userSettingsPrefs.edit {
            putInt(maximumPlayTimeKey(currentUser), maxHours)
        }
    }

    override fun clearUser(steamId: SteamId) {
        userSettingsPrefs.edit {
            remove(playTimeKey(steamId))
            remove(maximumPlayTimeKey(steamId))
        }
    }

    /**
     * Migration of old EnPlayTimeFilter to new PlaytimeFilter.Type
     */
    override fun migrateEnPlayTimeFilter() {
        val key = playTimeKey(currentUser)
        // EnPlayTimeFilter.NotPlayedIn2Weeks.ordinal == 2
        if (userSettingsPrefs.getInt(key, -1) == 2) {
            savePlayTimeFilterType(PlaytimeFilter.Type.All)
        }
    }

    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.as64()}"
    private fun maximumPlayTimeKey(steamId: SteamId) = "maximumPlayTime-${steamId.as64()}"
}