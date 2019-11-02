package ru.luckycactus.steamroulette.data.user_settings

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.luckycactus.steamroulette.data.intLiveData
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository

class UserSettingsRepositoryImpl(
    private val userSettingsPrefs: SharedPreferences
) : UserSettingsRepository {

    override fun observePlayTimeFilter(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ): LiveData<EnPlayTimeFilter> =
        userSettingsPrefs.intLiveData(playTimeKey(steamId), filter.ordinal)
            .map { EnPlayTimeFilter.fromOrdinal(it) }

    override fun savePlayTimeFilter(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ) {
        userSettingsPrefs.edit {
            putInt(playTimeKey(steamId), filter.ordinal)
        }
    }

    override fun clearUser(steamId: SteamId) {
        userSettingsPrefs.edit {
            remove(playTimeKey(steamId))
        }
    }

    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.asSteam64()}"
}