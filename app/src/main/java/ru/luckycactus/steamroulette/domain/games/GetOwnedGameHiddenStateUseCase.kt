package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class GetOwnedGameHiddenStateUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
): SuspendUseCase<Long, Boolean>() {

    override suspend fun execute(params: Long): Boolean {
        return gamesRepository.getOwnedGameHiddenState(params)
    }
}