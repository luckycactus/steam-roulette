package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

interface GamesDataStore {

    interface Local: GamesDataStore {
        suspend fun saveOwnedGamesToCache(steam64: Long, games: List<OwnedGameEntity>)

        suspend fun getFilteredOwnedGamesIds(steam64: Long): List<Int>

        suspend fun getOwnedGames(steam64: Long): List<OwnedGame>

        suspend fun markGameAsHidden(steam64: Long, gameId: Int)

        suspend fun getOwnedGame(steam64: Long, appId: Int): OwnedGame

        suspend fun isUserHasOwnedGames(steam64: Long): Boolean
    }

    interface Remote: GamesDataStore {
        suspend fun getOwnedGames(steam64: Long): List<OwnedGameEntity>
    }
}