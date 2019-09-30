package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.games.GamesRepository

class UpdateUserAndGamesUseCase(
    private val userRepository: UserRepository,
    private val gamesRepository: GamesRepository
) : SuspendUseCase<UpdateUserAndGamesUseCase.Params, Unit?>() {

    override suspend fun getResult(params: Params): Unit? =
        supervisorScope {
            val userRefreshAsync =
                async { userRepository.refreshUserSummary(params.steamId, CachePolicy.REMOTE) }
            val gamesUpdateException = try {
                gamesRepository.fetchOwnedGames(params.steamId, CachePolicy.REMOTE)
                null
            } catch (e: Exception) {
                e
            }
            val userUpdateException = try {
                userRefreshAsync.await()
                null
            } catch (e: Exception) {
                e
            }
            if (gamesUpdateException != null || userUpdateException != null) {
                throw UpdateException(
                    userUpdateException,
                    gamesUpdateException
                )
            }
        }


    data class Params(
        val steamId: SteamId
    )

    class UpdateException(
        val userUpdateException: Exception?,
        val gamesUpdateException: Exception?
    ) : Exception()
}