package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.data.games.SteamGamesRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId

class FetchOwnedGamesUseCase(
    private val gamesRepository: SteamGamesRepository
) : SuspendUseCase<FetchOwnedGamesUseCase.Params, List<Int>>() {

    override suspend fun getResult(params: Params): List<Int> {
        return gamesRepository.getOwnedGamesNumbers(
            params.steamId,
            if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        )
    }

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}