package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

class GetGameStoreInfoUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetGameStoreInfoUseCase.Params, GameStoreInfo>() {

    override suspend fun getResult(params: Params): GameStoreInfo {
        return gamesRepository.getGameStoreInfo(
            params.gameId,
            if (params.reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
        )!!
    }

    suspend fun getFromCache(gameId: Int): GameStoreInfo? {
        return gamesRepository.getGameStoreInfo(
            gameId,
            CachePolicy.OnlyCache
        )
    }

    data class Params(
        val gameId: Int,
        val reload: Boolean
    )
}