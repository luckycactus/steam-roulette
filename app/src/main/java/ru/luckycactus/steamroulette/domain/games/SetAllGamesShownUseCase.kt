package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import javax.inject.Inject

@Reusable
class SetAllGamesShownUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetAllGamesShownUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.setAllLocalOwnedGamesShown(params.steamId, params.shown)
    }

    data class Params(
        val steamId: SteamId,
        val shown: Boolean
    )
}