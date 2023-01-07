package ru.luckycactus.steamroulette.domain.games

import javax.inject.Inject

class SetGamesShownUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    suspend operator fun invoke(gameIds: List<Int>, shown: Boolean) {
        gamesRepository.setOwnedGamesShown(gameIds, shown)
    }
}