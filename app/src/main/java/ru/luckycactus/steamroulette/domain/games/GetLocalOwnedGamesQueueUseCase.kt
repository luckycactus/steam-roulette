package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.*
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException

class GetLocalOwnedGamesQueueUseCase(
    private val gamesRepository: GamesRepository,
    private val gameCoverPreloader: GameCoverPreloader
) : SuspendUseCase<GetLocalOwnedGamesQueueUseCase.Params, OwnedGamesQueue>() {

    override suspend fun getResult(params: Params): OwnedGamesQueue {
        if (!gamesRepository.isUserHasGames(params.steamId)) {
            throw MissingOwnedGamesException()
        }

        return OwnedGamesQueueImpl(
            params.steamId,
            gamesRepository.getFilteredLocalOwnedGamesIds(params.steamId, params.filter),
            gamesRepository,
            gameCoverPreloader
        )
    }

    data class Params (
        val steamId: SteamId,
        val filter: EnPlayTimeFilter
    )
}