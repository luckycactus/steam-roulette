package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import javax.inject.Inject

@Reusable
class SetAllGamesHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : AbstractSuspendUseCase<SetAllGamesHiddenUseCase.Params, Unit>() {
    override suspend fun execute(params: Params) {
        gamesRepository.setAllLocalOwnedGamesHidden(params.steamId, params.hide)
    }

    data class Params(
        val steamId: SteamId,
        val hide: Boolean
    )
}