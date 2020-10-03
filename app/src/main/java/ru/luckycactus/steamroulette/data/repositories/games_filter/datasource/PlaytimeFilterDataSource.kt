package ru.luckycactus.steamroulette.data.repositories.games_filter.datasource

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface PlaytimeFilterDataSource {
    fun observeFilterType(
        steamId: SteamId,
        default: PlaytimeFilter.Type
    ): Flow<PlaytimeFilter.Type>

    fun observeFilterType(steamId: SteamId): Flow<PlaytimeFilter.Type?>
    fun observeMaxHours(steamId: SteamId, default: Int): Flow<Int>
    fun saveFilterType(steamId: SteamId, filterType: PlaytimeFilter.Type)
    fun saveMaxPlaytime(steamId: SteamId, maxHours: Int)
    fun saveFilter(steamId: SteamId, filter: PlaytimeFilter)
    fun clear(steamId: SteamId)
    fun migrateEnPlayTimeFilter(steamId: SteamId)

    fun observeFilter(
        steamId: SteamId,
        defaultType: PlaytimeFilter.Type,
        defaultMaxHours: Int
    ): Flow<PlaytimeFilter>

    fun observeFilter(steamId: SteamId): Flow<PlaytimeFilter?>
}