package ru.luckycactus.steamroulette.domain.games_filter

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter

interface GamesFilterRepository {
    suspend fun saveFilter(filter: GamesFilter)
    fun observeFilter(default: GamesFilter): Flow<GamesFilter>
    fun observeMaxHours(default: Int): Flow<Int>
    fun clearUser(steamId: SteamId)
}

interface LibraryFilterRepository: GamesFilterRepository

interface RouletteFilterRepository: GamesFilterRepository