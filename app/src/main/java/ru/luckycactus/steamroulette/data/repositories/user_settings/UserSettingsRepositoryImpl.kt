package ru.luckycactus.steamroulette.data.repositories.user_settings

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.core.intFlow
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class UserSettingsRepositoryImpl @Inject constructor(
    @Identified(R.id.userSettingsPrefs) private val userSettingsPrefs: SharedPreferences
) : UserSettingsRepository {

    override fun observePlaytimeFilterType(
        steamId: SteamId,
        default: PlaytimeFilter.Type
    ): Flow<PlaytimeFilter.Type> =
        userSettingsPrefs.intFlow(playTimeKey(steamId), default.ordinal)
            .map { PlaytimeFilter.Type.fromOrdinal(it) }

    override fun observeMaxPlaytime(steamId: SteamId, default: Int): Flow<Int> =
        userSettingsPrefs.intFlow(maximumPlayTimeKey(steamId), default)

    override fun savePlayTimeFilterType(
        steamId: SteamId,
        filterType: PlaytimeFilter.Type
    ) {
        userSettingsPrefs.edit {
            putInt(playTimeKey(steamId), filterType.ordinal)
        }
    }

    override fun saveMaxPlaytime(steamId: SteamId, maxHours: Int) {
        userSettingsPrefs.edit {
            putInt(maximumPlayTimeKey(steamId), maxHours)
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
    override fun migrateEnPlayTimeFilter(steamId: SteamId) {
        val key = playTimeKey(steamId)
        // EnPlayTimeFilter.NotPlayedIn2Weeks.ordinal == 2
        if (userSettingsPrefs.getInt(key, -1) == 2) {
            savePlayTimeFilterType(steamId, PlaytimeFilter.Type.All)
        }
    }

    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.asSteam64()}"
    private fun maximumPlayTimeKey(steamId: SteamId) = "maximumPlayTime-${steamId.asSteam64()}"
}