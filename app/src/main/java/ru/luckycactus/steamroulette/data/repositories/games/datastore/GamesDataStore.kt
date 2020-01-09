package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesDataStore {

    interface Local : GamesDataStore {
        suspend fun saveOwnedGames(steam64: Long, gamesFlow: Flow<OwnedGameEntity>)

        suspend fun getOwnedGamesIds(steam64: Long, filter: PlaytimeFilter): List<Int>

        suspend fun hideOwnedGame(steam64: Long, gameId: Int)

        suspend fun getOwnedGame(steam64: Long, gameId: Int): OwnedGame

        suspend fun getOwnedGames(steam64: Long, gameIds: List<Int>): List<OwnedGame>

        suspend fun isUserHasGames(steam64: Long): Boolean

        fun observeOwnedGamesCount(steam64: Long): LiveData<Int>

        fun observeHiddenOwnedGamesCount(steam64: Long): LiveData<Int>

        suspend fun resetHiddenOwnedGames(steam64: Long)

        suspend fun clearOwnedGames(steam64: Long)
    }

    interface Remote : GamesDataStore {
        suspend fun getOwnedGames(steam64: Long): Flow<OwnedGameEntity>
    }
}