package ru.luckycactus.steamroulette.domain.games_filter

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface RouletteFiltersRepository {

    fun observePlaytimeFilterType(default: PlaytimeFilter.Type): Flow<PlaytimeFilter.Type>

    fun savePlaytimeFilter(filter: PlaytimeFilter)

    fun observeMaxPlaytime(default: Int): Flow<Int>

    fun observePlaytimeFilter(
        defaultType: PlaytimeFilter.Type,
        defaultMaxHours: Int
    ): Flow<PlaytimeFilter>

    fun clearUser(steamId: SteamId)

    fun migrateEnPlayTimeFilter()
}