package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import javax.inject.Inject

@Reusable
class SetGamesShownUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetGamesShownUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.setLocalOwnedGamesShown(params.steamId, params.gameIds, params.shown)
    }

    data class Params(
        val steamId: SteamId,
        val gameIds: List<Int>,
        val shown: Boolean
    )
}