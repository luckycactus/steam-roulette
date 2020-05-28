package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesRepository {

    @Throws(GetOwnedGamesPrivacyException::class)
    suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun getVisibleLocalOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter,
        shown: Boolean
    ): List<Int>

    suspend fun getLocalOwnedGameHeaders(steamId: SteamId, gameIds: List<Int>): List<GameHeader>

    suspend fun setLocalOwnedGamesHidden(steamId: SteamId, gameIds: List<Int>, hide: Boolean)

    suspend fun setAllLocalOwnedGamesHidden(steamId: SteamId, hide: Boolean)

    suspend fun setLocalOwnedGamesShown(steamId: SteamId, gameIds: List<Int>, shown: Boolean)

    suspend fun setAllLocalOwnedGamesShown(steamId: SteamId, shown: Boolean)

    suspend fun isUserHasGames(steamId: SteamId): Boolean

    fun observeGamesCount(steamId: SteamId): Flow<Int>

    fun observeHiddenGamesCount(steamId: SteamId): Flow<Int>

    //todo flow
    fun getHiddenGamesPagedListLiveData(steamId: SteamId): LiveData<PagedList<GameHeader>>

    suspend fun resetHiddenGames(steamId: SteamId)

    fun observeGamesUpdates(steamId: SteamId): Flow<Long>

    suspend fun clearUser(steamId: SteamId)

    @Throws(GetGameStoreInfoException::class)
    suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo?
}

class GetOwnedGamesPrivacyException: Exception()

class GetGameStoreInfoException: Exception()

