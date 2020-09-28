package ru.luckycactus.steamroulette.data.repositories.games.details.datasource

import ru.luckycactus.steamroulette.data.repositories.games.details.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException

interface GameStoreDataSource {

    interface Remote: GameStoreDataSource {
        @Throws(GetGameStoreInfoException::class)
        suspend fun get(appId: Int): GameStoreInfoEntity
    }
}