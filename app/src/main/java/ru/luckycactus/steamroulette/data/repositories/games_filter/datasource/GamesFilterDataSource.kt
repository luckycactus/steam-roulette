package ru.luckycactus.steamroulette.data.repositories.games_filter.datasource

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId

interface GamesFilterDataSource {
    fun observeHidden(steamId: SteamId): Flow<Boolean?>
    fun observeShown(steamId: SteamId): Flow<Boolean?>
    fun save(steamId: SteamId, hidden: Boolean?, shown: Boolean?)
}