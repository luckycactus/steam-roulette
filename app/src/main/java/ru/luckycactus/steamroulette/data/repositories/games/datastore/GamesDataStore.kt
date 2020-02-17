package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesDataStore {

    interface Local : GamesDataStore {
        suspend fun saveOwnedGames(steamId: SteamId, gamesFlow: Flow<OwnedGameEntity>)

        suspend fun getOwnedGamesIds(steamId: SteamId, filter: PlaytimeFilter): List<Int>

        suspend fun hideOwnedGame(steamId: SteamId, gameId: Int)

        suspend fun getOwnedGame(steamId: SteamId, gameId: Int): OwnedGame

        suspend fun getOwnedGames(steamId: SteamId, gameIds: List<Int>): List<OwnedGame>

        suspend fun isUserHasGames(steamId: SteamId): Boolean

        fun observeOwnedGamesCount(steamId: SteamId): LiveData<Int>

        fun observeHiddenOwnedGamesCount(steamId: SteamId): LiveData<Int>

        suspend fun resetHiddenOwnedGames(steamId: SteamId)

        suspend fun clearOwnedGames(steamId: SteamId)
    }

    interface Remote : GamesDataStore {
        suspend fun getOwnedGames(steamId: SteamId): Flow<OwnedGameEntity>

        suspend fun getGameStoreInfo(appId: Int): GameStoreInfo
    }
}