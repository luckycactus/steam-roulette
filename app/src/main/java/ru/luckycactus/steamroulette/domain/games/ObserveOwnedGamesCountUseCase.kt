package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import javax.inject.Inject

@Reusable
class ObserveOwnedGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
): AbstractUseCase<ObserveOwnedGamesCountUseCase.Params, Flow<Int>>() {

    override fun execute(params: Params): Flow<Int> {
        return gamesRepository.observeGamesCount(params.steamId)
    }

    data class Params(
        val steamId: SteamId
    )
}