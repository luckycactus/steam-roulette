package ru.luckycactus.steamroulette.domain.user_settings

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface UserSettingsRepository {

    fun observePlaytimeFilterType(default: PlaytimeFilter.Type): Flow<PlaytimeFilter.Type>

    fun savePlayTimeFilterType(filterType: PlaytimeFilter.Type)

    fun saveMaxPlaytime(maxHours: Int)

    fun observeMaxPlaytime(default: Int): Flow<Int>

    fun clearUser(steamId: SteamId)

    fun migrateEnPlayTimeFilter()
}