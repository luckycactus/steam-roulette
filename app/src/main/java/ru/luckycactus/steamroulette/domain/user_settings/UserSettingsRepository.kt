package ru.luckycactus.steamroulette.domain.user_settings

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId

interface UserSettingsRepository {

    fun observePlaytimeFilterType(
        steamId: SteamId,
        default: PlaytimeFilter.Type
    ): LiveData<PlaytimeFilter.Type>

    fun savePlayTimeFilterType(steamId: SteamId, filterType: PlaytimeFilter.Type)

    fun saveMaxPlaytime(steamId: SteamId, maxHours: Int)

    fun observeMaxPlaytime(steamId: SteamId, default: Int): LiveData<Int>

    fun clearUser(steamId: SteamId)
}