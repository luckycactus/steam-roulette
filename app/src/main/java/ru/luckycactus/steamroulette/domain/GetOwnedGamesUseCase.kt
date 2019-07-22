package ru.luckycactus.steamroulette.domain

import ru.luckycactus.steamroulette.data.games.SteamGamesRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase

class GetOwnedGamesUseCase(
    private val steamGamesRepository: SteamGamesRepository
): SuspendUseCase<GetOwnedGamesUseCase.Params, List<OwnedGame>>() {

    override suspend fun getResult(params: Params): List<OwnedGame> =
        steamGamesRepository.getOwnedGames(params.userId, params.reload)

    data class Params (
        val userId: Long,
        val reload: Boolean
    )
}