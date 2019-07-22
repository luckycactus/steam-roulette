package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity

class LocalSteamGamesDataStore : SteamGamesDataStore {

    override suspend fun getOwnedGames(
        userId: Long,
        includeAppInfo: Boolean,
        includePlayedFreeGames: Boolean
    ): List<OwnedGameEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}