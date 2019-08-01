package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId

interface GamesRepository {

    suspend fun getOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): List<OwnedGame>

    suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun getFilteredLocalOwnedGamesIds(steamId: SteamId): List<Int>

    suspend fun getLocalOwnedGame(steamId: SteamId, appId: Int): OwnedGame

    suspend fun markLocalGameAsHidden(steamId: SteamId, ownedGame: OwnedGame)

    suspend fun isUserHasLocalOwnedGames(steamId: SteamId): Boolean
}
