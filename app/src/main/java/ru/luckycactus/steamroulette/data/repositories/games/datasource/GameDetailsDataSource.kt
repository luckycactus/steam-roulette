package ru.luckycactus.steamroulette.data.repositories.games.datasource

import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException

interface GameDetailsDataSource {

    interface Remote: GameDetailsDataSource {
        @Throws(GetGameStoreInfoException::class)
        suspend fun getGameStoreInfo(appId: Int): GameStoreInfoEntity
    }
}