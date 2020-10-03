package ru.luckycactus.steamroulette.domain.games_filter

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter

interface LibraryFiltersRepository {
    suspend fun saveFilter(filter: GamesFilter)
    fun observeFilter(default: GamesFilter): Flow<GamesFilter>
    fun observeMaxHours(default: Int): Flow<Int>
}