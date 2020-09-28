package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

@Reusable
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