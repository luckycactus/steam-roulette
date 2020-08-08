package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.paging.DataSource
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesDataStore {

    interface Local : GamesDataStore {
        suspend fun updateOwnedGames(steamId: SteamId, gamesFlow: Flow<OwnedGameEntity>)

        suspend fun getOwnedGames(steamId: SteamId): List<OwnedGameEntity>

        suspend fun getOwnedGamesIds(
            steamId: SteamId,
            shown: Boolean? = null,
            hidden: Boolean? = null,
            filter: PlaytimeFilter? = null
        ): List<Int>

        suspend fun setOwnedGamesHidden(steamId: SteamId, gameIds: List<Int>, hide: Boolean)

        suspend fun setAllOwnedGamesHidden(steamId: SteamId, hide: Boolean)

        suspend fun setOwnedGamesShown(steamId: SteamId, gameIds: List<Int>, shown: Boolean)

        suspend fun setAllOwnedGamesShown(steamId: SteamId, shown: Boolean)

        suspend fun getOwnedGameHeader(steamId: SteamId, gameId: Int): GameHeader

        suspend fun getOwnedGameHeaders(steamId: SteamId, gameIds: List<Int>): List<GameHeader>

        suspend fun isUserHasGames(steamId: SteamId): Boolean

        fun observeOwnedGamesCount(steamId: SteamId): Flow<Int>

        fun observeHiddenOwnedGamesCount(steamId: SteamId): Flow<Int>

        suspend fun resetHiddenOwnedGames(steamId: SteamId)

        suspend fun clearOwnedGames(steamId: SteamId)

        fun getHiddenGamesPagingSource(steamId: SteamId): PagingSource<Int, GameHeader>
    }

    interface Remote : GamesDataStore {
        suspend fun getOwnedGames(steamId: SteamId): Flow<OwnedGameEntity>

        @Throws(GetGameStoreInfoException::class)
        suspend fun getGameStoreInfo(appId: Int): GameStoreInfoEntity
    }
}