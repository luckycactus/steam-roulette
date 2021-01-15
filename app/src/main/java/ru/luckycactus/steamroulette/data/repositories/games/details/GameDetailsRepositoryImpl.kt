package ru.luckycactus.steamroulette.data.repositories.games.details

import dagger.Reusable
import ru.luckycactus.steamroulette.data.core.MemoryCacheNbr
import ru.luckycactus.steamroulette.data.repositories.games.details.datasource.GameStoreDataSource
import ru.luckycactus.steamroulette.data.repositories.games.details.mapper.GameStoreInfoEntityMapper
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.GameDetailsRepository
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GameDetailsRepositoryImpl @Inject constructor(
    private val remoteGameStoreDataSource: GameStoreDataSource.Remote,
    private val gameStoreInfoEntityMapper: GameStoreInfoEntityMapper
) : GameDetailsRepository {

    override suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo? {
        val resource = object : MemoryCacheNbr<GameStoreInfo, GameStoreInfo>(
            memoryKey = "game_store_info_$gameId"
        ) {
            override suspend fun fetch(): GameStoreInfo =
                gameStoreInfoEntityMapper.mapFrom(
                    remoteGameStoreDataSource.get(gameId)
                )
        }
        return resource.get(cachePolicy)
    }
}