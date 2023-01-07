package ru.luckycactus.steamroulette.domain.games

import javax.inject.Inject

class SetGamesHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    suspend operator fun invoke(gameIds: List<Int>, hide: Boolean) {
        gamesRepository.setOwnedGamesHidden(gameIds, hide)
    }
}