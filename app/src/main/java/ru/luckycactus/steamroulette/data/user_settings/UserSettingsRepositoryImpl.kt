package ru.luckycactus.steamroulette.data.user_settings

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.intLiveData
import ru.luckycactus.steamroulette.di.common.Identified
import ru.luckycactus.steamroulette.domain.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class UserSettingsRepositoryImpl @Inject constructor(
    @Identified(R.id.userSettingsPrefs) private val userSettingsPrefs: SharedPreferences
) : UserSettingsRepository {

    override fun observePlaytimeFilterType(
        steamId: SteamId,
        default: PlaytimeFilter.Type
    ): LiveData<PlaytimeFilter.Type> =
        userSettingsPrefs.intLiveData(playTimeKey(steamId), default.ordinal)
            .map { PlaytimeFilter.Type.fromOrdinal(it) }

    override fun observeMaxPlaytime(steamId: SteamId, default: Int): LiveData<Int> =
        userSettingsPrefs.intLiveData(maximumPlayTimeKey(steamId), default)

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
        }
    }

    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.asSteam64()}"
    private fun maximumPlayTimeKey(steamId: SteamId) = "maximumPlayTime-${steamId.asSteam64()}"
}