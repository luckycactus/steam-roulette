package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

@Reusable
class ClearHiddenGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit) {
        gamesRepository.resetHiddenGames()
    }
}