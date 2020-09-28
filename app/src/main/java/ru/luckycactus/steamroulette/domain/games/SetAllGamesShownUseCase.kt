package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

@Reusable
class SetAllGamesShownUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetAllGamesShownUseCase.Params, Unit>() {

    override suspend fun execute(params: Params) {
        gamesRepository.setAllLocalOwnedGamesShown(params.shown)
    }

    data class Params(
        val shown: Boolean
    )
}