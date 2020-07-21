package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import javax.inject.Inject

@Reusable
class ObserveOwnedGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : AbstractUseCase<Unit, Flow<Int>>() {

    override fun execute(params: Unit): Flow<Int> {
        return gamesRepository.observeGamesCount()
    }
}