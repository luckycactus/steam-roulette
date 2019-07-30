package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId

interface SteamGamesRepository {

    suspend fun getOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): List<OwnedGame>

    suspend fun getOwnedGamesNumbers(steamId: SteamId, cachePolicy: CachePolicy): List<Int>

    suspend fun getOwnedGameByNumber(number: Int): OwnedGame

    suspend fun markGameAsHidden(steamId: SteamId, ownedGame: OwnedGame)
}
