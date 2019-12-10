package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import javax.inject.Inject

@Reusable
class HideGameUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<HideGameUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.markLocalGameAsHidden(params.steamId, params.gameId)
    }

    data class Params(
        val steamId: SteamId,
        val gameId: Int
    )
}