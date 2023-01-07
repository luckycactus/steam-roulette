package ru.luckycactus.steamroulette.domain.games

import javax.inject.Inject

class GetOwnedGameHiddenStateUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    suspend operator fun invoke(params: Long): Boolean {
        return gamesRepository.getOwnedGameHiddenState(params)
    }
}