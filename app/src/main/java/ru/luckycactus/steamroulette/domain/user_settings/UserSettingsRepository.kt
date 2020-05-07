package ru.luckycactus.steamroulette.domain.user_settings

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface UserSettingsRepository {

    fun observePlaytimeFilterType(
        steamId: SteamId,
        default: PlaytimeFilter.Type
    ): Flow<PlaytimeFilter.Type>

    fun savePlayTimeFilterType(steamId: SteamId, filterType: PlaytimeFilter.Type)

    fun saveMaxPlaytime(steamId: SteamId, maxHours: Int)

    fun observeMaxPlaytime(steamId: SteamId, default: Int): Flow<Int>

    fun clearUser(steamId: SteamId)

    fun migrateEnPlayTimeFilter(steamId: SteamId)
}