package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueueImpl
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.exception.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.exception.NetworkConnectionException
import ru.luckycactus.steamroulette.domain.exception.ServerException
import java.lang.Exception
import java.lang.ref.WeakReference

class GetOwnedGamesQueueUseCase(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetOwnedGamesQueueUseCase.Params, OwnedGamesQueue>() {

    private var lastQueue: WeakReference<OwnedGamesQueueImpl>? = null

    override suspend fun getResult(params: Params): OwnedGamesQueue {
        val cachePolicy = if (params.reload) {
            CachePolicy.REMOTE
        } else if (!gamesRepository.isUserHasLocalOwnedGames(params.steamId)) {
            CachePolicy.REMOTE
        } else {
            CachePolicy.ONLY_CACHE
        }

        lastQueue?.get()?.invalidate()

        gamesRepository.fetchOwnedGames(
            params.steamId,
            cachePolicy
        )

        if (!gamesRepository.isUserHasLocalOwnedGames(params.steamId)) {
            throw MissingOwnedGamesException()
        }

        val gameNumbers = gamesRepository.getFilteredLocalOwnedGamesIds(params.steamId)
        val queue = OwnedGamesQueueImpl(
            params.steamId,
            gameNumbers,
            gamesRepository
        )
        lastQueue = WeakReference(queue)
        return queue
    }

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}