package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesDataStore {

    interface Local : GamesDataStore {
        suspend fun saveOwnedGames(steamId: SteamId, gamesFlow: Flow<OwnedGameEntity>)

        suspend fun getOwnedGamesIds(steamId: SteamId, filter: PlaytimeFilter): List<Int>

        suspend fun setOwnedGameHidden(steamId: SteamId, gameId: Int, hide: Boolean)

        suspend fun getOwnedGameHeader(steamId: SteamId, gameId: Int): GameHeader

        suspend fun getOwnedGameHeaders(steamId: SteamId, gameIds: List<Int>): List<GameHeader>

        suspend fun isUserHasGames(steamId: SteamId): Boolean

        fun observeOwnedGamesCount(steamId: SteamId): LiveData<Int>

        fun observeHiddenOwnedGamesCount(steamId: SteamId): LiveData<Int>

        suspend fun resetHiddenOwnedGames(steamId: SteamId)

        suspend fun clearOwnedGames(steamId: SteamId)

        fun getHiddenGamesDataSourceFactory(steamId: SteamId): DataSource.Factory<Int, GameHeader>
    }

    interface Remote : GamesDataStore {
        suspend fun getOwnedGames(steamId: SteamId): Flow<OwnedGameEntity>

        suspend fun getGameStoreInfo(appId: Int): GameStoreInfoEntity
    }
}