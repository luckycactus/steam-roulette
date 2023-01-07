package ru.luckycactus.steamroulette.domain.games

import javax.inject.Inject

class SetAllGamesShownUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    suspend operator fun invoke(shown: Boolean) {
        gamesRepository.setAllOwnedGamesShown(shown)
    }
}