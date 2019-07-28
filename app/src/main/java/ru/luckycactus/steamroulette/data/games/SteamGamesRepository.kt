package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.domain.CachePolicy
import ru.luckycactus.steamroulette.domain.OwnedGame

interface SteamGamesRepository {

    suspend fun getOwnedGames(steam64: Long, cachePolicy: CachePolicy): List<OwnedGame>
}
