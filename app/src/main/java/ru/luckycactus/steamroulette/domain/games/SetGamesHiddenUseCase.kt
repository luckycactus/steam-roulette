package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class SetGamesHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetGamesHiddenUseCase.Params, Unit>() {

    override suspend fun execute(params: Params) {
        gamesRepository.setOwnedGamesHidden(params.gameIds, params.hide)
    }

    data class Params(
        val gameIds: List<Int>,
        val hide: Boolean
    )
}