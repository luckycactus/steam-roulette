package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo

interface GameDetailsRepository {

    @Throws(GetGameStoreInfoException::class)
    suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo?
}