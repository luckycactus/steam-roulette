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
        default: EnPlayTimeFilter
    ): LiveData<EnPlayTimeFilter> {
        return userSettingsPrefs.intLiveData(playTimeKey(steamId), default.ordinal)
            .map { EnPlayTimeFilter.fromOrdinal(it) }
    }

    override fun savePlayTimeFilter(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ) {
        userSettingsPrefs.edit {
            putInt(playTimeKey(steamId), filter.ordinal)
        }
    }

    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.asSteam64()}"
}