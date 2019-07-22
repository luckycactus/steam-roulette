package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity

interface SteamGamesDataStore {

    suspend fun getOwnedGames(
        userId: Long,
        includeAppInfo: Boolean,
        includePlayedFreeGames: Boolean
    ): List<OwnedGameEntity>
}