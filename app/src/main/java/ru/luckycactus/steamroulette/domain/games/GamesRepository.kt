package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesRepository {

    suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun getLocalOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter
    ): List<Int>

    suspend fun getLocalOwnedGameHeaders(steamId: SteamId, gameIds: List<Int>): List<GameHeader>

    suspend fun setLocalOwnedGameHidden(steamId: SteamId, gameId: Int, hide: Boolean)

    suspend fun isUserHasGames(steamId: SteamId): Boolean

    fun observeGamesCount(steamId: SteamId): LiveData<Int>

    fun observeHiddenGamesCount(steamId: SteamId): LiveData<Int>

    fun getHiddenGamesPagedListLiveData(steamId: SteamId): LiveData<PagedList<GameHeader>>

    suspend fun resetHiddenGames(steamId: SteamId)

    fun observeGamesUpdates(steamId: SteamId): LiveData<Long>

    suspend fun clearUser(steamId: SteamId)

    suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo?
}

