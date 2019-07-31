package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class GetOwnedGamesUseCase(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetOwnedGamesUseCase.Params, List<OwnedGame>>() {

    override suspend fun getResult(params: Params): List<OwnedGame> =
        gamesRepository.getOwnedGames(
            params.steamId,
            if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        )

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}