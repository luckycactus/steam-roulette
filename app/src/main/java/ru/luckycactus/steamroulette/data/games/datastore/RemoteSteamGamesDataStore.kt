package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.wrapCommonNetworkExceptions

class RemoteSteamGamesDataStore(
    private val steamApiService: SteamApiService
) : SteamGamesDataStore.Remote {

    override suspend fun getOwnedGames(steam64: Long): List<OwnedGameEntity> =
        wrapCommonNetworkExceptions {
            steamApiService.getOwnedGames(
                steam64,
                includeAppInfo = true,
                includePlayedFreeGames = false
            ).ownedGameEntity.games
        }

}