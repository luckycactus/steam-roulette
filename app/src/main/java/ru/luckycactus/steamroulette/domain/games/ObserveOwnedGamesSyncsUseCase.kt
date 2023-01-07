package ru.luckycactus.steamroulette.domain.games

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOwnedGamesSyncsUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(): Flow<Long> {
        return gamesRepository.observeGamesUpdates()
    }
}