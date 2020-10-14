package ru.luckycactus.steamroulette.domain.games

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter

interface GamesRepository {

    @Throws(GetOwnedGamesPrivacyException::class)
    suspend fun fetchOwnedGames(cachePolicy: CachePolicy)

    suspend fun getOwnedGamesIds(gamesFilter: GamesFilter): List<Int>

    suspend fun getLocalOwnedGameHeaders(gameIds: List<Int>): List<GameHeader>

    suspend fun setLocalOwnedGamesHidden(gameIds: List<Int>, hide: Boolean)

    suspend fun setAllLocalOwnedGamesHidden(hide: Boolean)

    suspend fun setLocalOwnedGamesShown(gameIds: List<Int>, shown: Boolean)

    suspend fun setAllLocalOwnedGamesShown(shown: Boolean)

    suspend fun isUserHasGames(): Boolean

    fun observeGamesCount(filter: GamesFilter): Flow<Int>

    suspend fun resetHiddenGames()

    fun observeGamesUpdates(): Flow<Long>

    suspend fun clearUser(steamId: SteamId)

    fun getLibraryPagingSource(
        filter: GamesFilter,
        nameSearchQuery: String?
    ): PagingSource<Int, LibraryGame>

    suspend fun getOwnedGameHiddenState(appId: Long): Boolean
}

class GetOwnedGamesPrivacyException : Exception()

class GetGameStoreInfoException : Exception()

