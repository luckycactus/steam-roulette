package ru.luckycactus.steamroulette.domain.games

interface RouletteRepository {
    suspend fun getLastTopGameId(): Int?
    suspend fun setLastTopGameId(appId: Int?)
    suspend fun clear()
}