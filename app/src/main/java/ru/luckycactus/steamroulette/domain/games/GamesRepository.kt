package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import java.util.*

interface GamesRepository {

    suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun getFilteredLocalOwnedGamesIds(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ): List<Int>

    @Deprecated("")
    suspend fun getFilteredLocalOwnedGames(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ): List<OwnedGame>

    suspend fun getLocalOwnedGame(steamId: SteamId, gameId: Int): OwnedGame

    suspend fun getLocalOwnedGames(steamId: SteamId, gameIds: List<Int>): List<OwnedGame>

    suspend fun markLocalGameAsHidden(steamId: SteamId, gameId: Int)

    suspend fun isUserHasGames(steamId: SteamId): Boolean

    fun observeGamesCount(steamId: SteamId): LiveData<Int>

    fun observeHiddenGamesCount(steamId: SteamId): LiveData<Int>

    suspend fun clearHiddenGames(steamId: SteamId)

    fun observeGamesUpdates(steamId: SteamId): LiveData<Long>

    suspend fun clearUser(steamId: SteamId)
}

