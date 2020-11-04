package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SteamId

interface RouletteRepository {
    suspend fun getLastTopGameId(): Int?
    suspend fun setLastTopGameId(appId: Int?)
    suspend fun clearUser(steamId: SteamId)
}