package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.UseCase
import javax.inject.Inject

@Reusable
class ObserveOwnedGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
): UseCase<ObserveOwnedGamesCountUseCase.Params, Flow<Int>>() {

    override fun getResult(params: Params): Flow<Int> {
        return gamesRepository.observeGamesCount(params.steamId)
    }

    data class Params(
        val steamId: SteamId
    )
}