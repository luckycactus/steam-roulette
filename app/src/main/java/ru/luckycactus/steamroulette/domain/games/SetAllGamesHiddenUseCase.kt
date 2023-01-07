package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.utils.extensions.cancellable
import javax.inject.Inject

class SetAllGamesHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    suspend operator fun invoke(hide: Boolean): Result<Unit> {
        return kotlin.runCatching {
            gamesRepository.setAllOwnedGamesHidden(hide)
        }.cancellable()
    }
}