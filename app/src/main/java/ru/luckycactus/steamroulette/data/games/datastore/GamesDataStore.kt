package ru.luckycactus.steamroulette.data.games.datastore

import androidx.lifecycle.LiveData
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

interface GamesDataStore {

    interface Local : GamesDataStore {
        suspend fun saveOwnedGames(steam64: Long, gamesFlow: Flow<OwnedGameEntity>)

        suspend fun getFilteredOwnedGamesIds(steam64: Long, filter: EnPlayTimeFilter): List<Int>

        suspend fun hideOwnedGame(steam64: Long, gameId: Int)

        suspend fun getOwnedGame(steam64: Long, gameId: Int): OwnedGame

        suspend fun getOwnedGames(steam64: Long, gameIds: List<Int>): List<OwnedGame>

        suspend fun isUserHasGames(steam64: Long): Boolean

        fun observeOwnedGamesCount(steam64: Long): LiveData<Int>

        fun observeHiddenOwnedGamesCount(steam64: Long): LiveData<Int>

        suspend fun clearHiddenOwnedGames(steam64: Long)
        suspend fun clearOwnedGames(steam64: Long)
    }

    interface Remote : GamesDataStore {
        suspend fun getOwnedGames(steam64: Long): Flow<OwnedGameEntity>
    }
}