package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity

class LocalSteamGamesDataStore : SteamGamesDataStore.Local {

    override suspend fun getOwnedGames(steam64: Long): List<OwnedGameEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveOwnedGamesToCache(games: List<OwnedGameEntity>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOwnedGamesCount(steam64: Long): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}