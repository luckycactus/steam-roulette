package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId

interface SteamGamesRepository {

    suspend fun getOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): List<OwnedGame>

    suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): Int
}
