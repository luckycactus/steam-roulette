package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueueImpl
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException

class GetLocalOwnedGamesQueueUseCase(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SteamId, OwnedGamesQueue>() {

    override suspend fun getResult(params: SteamId): OwnedGamesQueue {
        if (!gamesRepository.isUserHasLocalOwnedGames(params)) {
            throw MissingOwnedGamesException()
        }

        return OwnedGamesQueueImpl(
            params,
            gamesRepository.getFilteredLocalOwnedGamesIds(params),
            gamesRepository
        )
    }
}