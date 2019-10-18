package ru.luckycactus.steamroulette.data.games.datastore

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

interface GamesDataStore {

    interface Local: GamesDataStore {
        suspend fun saveOwnedGamesToCache(steam64: Long, games: List<OwnedGameEntity>)

        suspend fun getFilteredOwnedGamesIds(steam64: Long, filter: EnPlayTimeFilter): List<Int>

        suspend fun getOwnedGames(steam64: Long): List<OwnedGame>

        suspend fun markGameAsHidden(steam64: Long, gameId: Int)

        suspend fun getOwnedGame(steam64: Long, appId: Int): OwnedGame

        suspend fun getOwnedGames(steam64: Long, appIds: List<Int>): List<OwnedGame>

        suspend fun isUserHasOwnedGames(steam64: Long): Boolean

        fun observeGameCount(steam64: Long): LiveData<Int>

        fun observeHiddenGameCount(steam64: Long): LiveData<Int>

        suspend fun clearHiddenGames(steam64: Long)
    }

    interface Remote: GamesDataStore {
        suspend fun getOwnedGames(steam64: Long): List<OwnedGameEntity>
    }
}