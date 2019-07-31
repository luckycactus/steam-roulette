package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueueImpl
import ru.luckycactus.steamroulette.domain.entity.SteamId

class GetOwnedGamesQueueUseCase(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetOwnedGamesQueueUseCase.Params, OwnedGamesQueue>() {

    override suspend fun getResult(params: Params): OwnedGamesQueue {
        gamesRepository.fetchOwnedGames(
            params.steamId,
            if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        )
        val gameNumbers = gamesRepository.getLocalOwnedGamesNumbers(params.steamId)
        //todo error handling
        return OwnedGamesQueueImpl(
            params.steamId,
            gameNumbers,
            gamesRepository
        )
    }

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}