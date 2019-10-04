package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueueImpl
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException

class GetLocalOwnedGamesQueueUseCase(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetLocalOwnedGamesQueueUseCase.Params, OwnedGamesQueue>() {

    override suspend fun getResult(params: Params): OwnedGamesQueue {
        if (!gamesRepository.isUserHasLocalOwnedGames(params.steamId)) {
            throw MissingOwnedGamesException()
        }

        return OwnedGamesQueueImpl(
            params.steamId,
            gamesRepository.getFilteredLocalOwnedGamesIds(params.steamId, params.filter),
            gamesRepository
        )
    }

    data class Params (
        val steamId: SteamId,
        val filter: EnPlayTimeFilter
    )
}