package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class HideGameUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<HideGameUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.hideLocalOwnedGame(params.steamId, params.gameId)
    }

    data class Params(
        val steamId: SteamId,
        val gameId: Int
    )
}