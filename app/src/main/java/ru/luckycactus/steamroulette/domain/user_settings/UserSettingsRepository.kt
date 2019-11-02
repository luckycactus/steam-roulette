package ru.luckycactus.steamroulette.domain.user_settings

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId

interface UserSettingsRepository {

    fun observePlayTimeFilter(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ): LiveData<EnPlayTimeFilter>

    fun savePlayTimeFilter(steamId: SteamId, filter: EnPlayTimeFilter)

    fun clearUser(steamId: SteamId)
}