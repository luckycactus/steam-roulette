package ru.luckycactus.steamroulette.domain

import ru.luckycactus.steamroulette.data.games.SteamGamesRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.user.SteamId

class GetOwnedGamesUseCase(
    private val steamGamesRepository: SteamGamesRepository
) : SuspendUseCase<GetOwnedGamesUseCase.Params, List<OwnedGame>>() {

    override suspend fun getResult(params: Params): List<OwnedGame> =
        steamGamesRepository.getOwnedGames(
            params.steamId.asSteam64(),
            if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        )

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}