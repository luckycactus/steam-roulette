package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter

interface GamesDataSource {

    interface Local : GamesDataSource {
        suspend fun update(steamId: SteamId, gamesFlow: Flow<OwnedGameEntity>)

        suspend fun getAll(steamId: SteamId): List<OwnedGameEntity>

        suspend fun getIds(steamId: SteamId, filter: GamesFilter, orderById: Boolean = false): List<Int>

        suspend fun getIdsMutable(steamId: SteamId, filter: GamesFilter, orderById: Boolean = false): MutableList<Int>

        suspend fun setHidden(steamId: SteamId, gameIds: List<Int>, hide: Boolean)

        suspend fun setAllHidden(steamId: SteamId, hide: Boolean)

        suspend fun setShown(steamId: SteamId, gameIds: List<Int>, shown: Boolean)

        suspend fun setAllShown(steamId: SteamId, shown: Boolean)

        suspend fun getHeader(steamId: SteamId, gameId: Int): GameHeader

        suspend fun getHeaders(steamId: SteamId, gameIds: List<Int>): List<GameHeader>

        suspend fun isUserHasGames(steamId: SteamId): Boolean

        fun observeCount(steamId: SteamId, filter: GamesFilter): Flow<Int>

        suspend fun resetAllHidden(steamId: SteamId)

        suspend fun clear(steamId: SteamId)

        fun getLibraryPagingSource(
            steamId: SteamId,
            filter: GamesFilter,
            nameSearchQuery: String?
        ): PagingSource<Int, LibraryGame>

        suspend fun getHiddenState(steamId: SteamId, appId: Long): Boolean
    }

    interface Remote : GamesDataSource {
        suspend fun getAll(steamId: SteamId): Flow<OwnedGameEntity>
    }
}