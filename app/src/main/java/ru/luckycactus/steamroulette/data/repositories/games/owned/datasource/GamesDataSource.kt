package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesDataSource {

    interface Local : GamesDataSource {
        suspend fun update(steamId: SteamId, gamesFlow: Flow<OwnedGameEntity>)

        suspend fun getAll(steamId: SteamId): List<OwnedGameEntity>

        suspend fun getIds(
            steamId: SteamId,
            shown: Boolean? = null,
            hidden: Boolean? = null,
            filter: PlaytimeFilter? = null
        ): List<Int>

        suspend fun setHidden(steamId: SteamId, gameIds: List<Int>, hide: Boolean)

        suspend fun setAllHidden(steamId: SteamId, hide: Boolean)

        suspend fun setShown(steamId: SteamId, gameIds: List<Int>, shown: Boolean)

        suspend fun setAllShown(steamId: SteamId, shown: Boolean)

        suspend fun getHeader(steamId: SteamId, gameId: Int): GameHeader

        suspend fun getHeaders(steamId: SteamId, gameIds: List<Int>): List<GameHeader>

        suspend fun isUserHasGames(steamId: SteamId): Boolean

        fun observeCount(steamId: SteamId): Flow<Int>

        fun observeHiddenCount(steamId: SteamId): Flow<Int>

        suspend fun resetAllHidden(steamId: SteamId)

        suspend fun clear(steamId: SteamId)

        fun getPagingSource(
            steamId: SteamId,
            shown: Boolean?,
            hidden: Boolean?,
            filter: PlaytimeFilter?
        ): PagingSource<Int, GameHeader>
    }

    interface Remote : GamesDataSource {
        suspend fun getAll(steamId: SteamId): Flow<OwnedGameEntity>
    }
}