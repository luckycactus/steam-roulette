package ru.luckycactus.steamroulette.domain.games

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter

interface GamesRepository {

    @Throws(GetOwnedGamesPrivacyException::class)
    suspend fun updateOwnedGames(cachePolicy: CachePolicy)


    suspend fun getOwnedGamesIds(gamesFilter: GamesFilter, orderById: Boolean = false): List<Int>

    suspend fun getOwnedGamesIdsMutable(gamesFilter: GamesFilter, orderById: Boolean = false): MutableList<Int>

    suspend fun getOwnedGameHeaders(gameIds: List<Int>): List<GameHeader>

    fun observeGamesCount(filter: GamesFilter): Flow<Int>

    fun observeGamesUpdates(): Flow<Long>

    fun getLibraryPagingSource(
        filter: GamesFilter,
        nameSearchQuery: String?
    ): PagingSource<Int, LibraryGame>


    suspend fun setOwnedGamesHidden(gameIds: List<Int>, hide: Boolean)

    suspend fun setAllOwnedGamesHidden(hide: Boolean)

    suspend fun resetHiddenGames()

    suspend fun getOwnedGameHiddenState(appId: Long): Boolean


    suspend fun setOwnedGamesShown(gameIds: List<Int>, shown: Boolean)

    suspend fun setAllOwnedGamesShown(shown: Boolean)


    suspend fun isUserHasGames(): Boolean

    suspend fun clearUser(steamId: SteamId)
}

class GetOwnedGamesPrivacyException : Exception()

class GetGameStoreInfoException : Exception()

