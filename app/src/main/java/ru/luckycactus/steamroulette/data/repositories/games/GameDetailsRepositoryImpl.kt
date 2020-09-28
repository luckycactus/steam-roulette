package ru.luckycactus.steamroulette.data.repositories.games

import dagger.Reusable
import ru.luckycactus.steamroulette.data.core.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.games.datasource.GameDetailsDataSource
import ru.luckycactus.steamroulette.data.repositories.games.mapper.GameStoreInfoEntityMapper
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.GameDetailsRepository
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GameDetailsRepositoryImpl @Inject constructor(
    private val remoteGameDetailsDataSource: GameDetailsDataSource.Remote,
    private val gameStoreInfoEntityMapper: GameStoreInfoEntityMapper
) : GameDetailsRepository {

    override suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo? {
        return object : NetworkBoundResource.MemoryCache<GameStoreInfo, GameStoreInfo>(
            "game_store_info_$gameId"
        ) {
            override suspend fun fetch(): GameStoreInfo =
                gameStoreInfoEntityMapper.mapFrom(
                    remoteGameDetailsDataSource.getGameStoreInfo(gameId)
                )
        }.get(cachePolicy)
    }
}