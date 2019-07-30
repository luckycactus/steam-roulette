package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

interface SteamGamesDataStore {

    interface Local: SteamGamesDataStore {
        suspend fun saveOwnedGamesToCache(steam64: Long, games: List<OwnedGameEntity>)

        suspend fun getOwnedGamesNumbers(steam64: Long): List<Int>

        suspend fun getOwnedGames(steam64: Long): List<OwnedGame>

        suspend fun markGameAsHidden(steam64: Long, gameId: Long)

        suspend fun getOwnedGameByNumber(number: Int): OwnedGame
    }

    interface Remote: SteamGamesDataStore {
        suspend fun getOwnedGames(steam64: Long): List<OwnedGameEntity>
    }
}