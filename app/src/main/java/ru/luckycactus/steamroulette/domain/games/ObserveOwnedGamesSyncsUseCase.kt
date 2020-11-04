package ru.luckycactus.steamroulette.domain.games

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject

class ObserveOwnedGamesSyncsUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<Unit, Flow<Long>>() {

    override fun execute(params: Unit): Flow<Long> {
        return gamesRepository.observeGamesUpdates()
    }
}