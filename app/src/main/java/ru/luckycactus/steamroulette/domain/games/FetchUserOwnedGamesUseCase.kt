package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId

class FetchUserOwnedGamesUseCase(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<FetchUserOwnedGamesUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.fetchOwnedGames(
            params.steamId,
            if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        )
    }

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}