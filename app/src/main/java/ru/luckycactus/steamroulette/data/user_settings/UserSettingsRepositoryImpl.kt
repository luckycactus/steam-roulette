package ru.luckycactus.steamroulette.data.user_settings

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.intLiveData
import ru.luckycactus.steamroulette.di.common.Identified
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class UserSettingsRepositoryImpl @Inject constructor(
    @Identified(R.id.userSettingsPrefs) private val userSettingsPrefs: SharedPreferences
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