package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.presentation.nullIfFalse

class RemoteSteamGamesDataStore(
    private val steamApiService: SteamApiService
) : SteamGamesDataStore {

    //todo cache
    override suspend fun getOwnedGames(
        userId: Long,
        includeAppInfo: Boolean,
        includePlayedFreeGames: Boolean
    ): List<OwnedGameEntity> =
        steamApiService.getOwnedGames(
            userId,
            includeAppInfo.nullIfFalse(),
            includePlayedFreeGames.nullIfFalse()
        ).ownedGameEntity.games

}