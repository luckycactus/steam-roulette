package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class SetAllGamesHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetAllGamesHiddenUseCase.Params, Unit>() {
    override suspend fun execute(params: Params) {
        gamesRepository.setAllLocalOwnedGamesHidden(params.hide)
    }

    data class Params(
        val hide: Boolean
    )
}