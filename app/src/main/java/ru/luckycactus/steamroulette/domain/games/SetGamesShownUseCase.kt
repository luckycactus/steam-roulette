package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class SetGamesShownUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetGamesShownUseCase.Params, Unit>() {

    override suspend fun execute(params: Params) {
        gamesRepository.setLocalOwnedGamesShown(params.gameIds, params.shown)
    }

    data class Params(
        val gameIds: List<Int>,
        val shown: Boolean
    )
}