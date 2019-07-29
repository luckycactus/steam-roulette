package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity

interface SteamGamesDataStore {

    suspend fun getOwnedGames(steam64: Long): List<OwnedGameEntity>

    interface Local: SteamGamesDataStore {
        fun saveOwnedGamesToCache(games: List<OwnedGameEntity>)

        fun getOwnedGamesCount(steam64: Long): Int
    }

    interface Remote: SteamGamesDataStore
}