package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GetGameStoreInfoUseCase @Inject constructor(
    private val gameDetailsRepository: GameDetailsRepository
) {
    suspend operator fun invoke(
        gameId: Int,
        cachePolicy: CachePolicy = CachePolicy.CacheOrRemote
    ): GameStoreInfo? {
        return gameDetailsRepository.getGameStoreInfo(gameId, cachePolicy)
    }
}