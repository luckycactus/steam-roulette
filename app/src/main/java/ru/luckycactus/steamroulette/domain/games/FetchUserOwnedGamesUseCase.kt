package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.common.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class FetchUserOwnedGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<FetchUserOwnedGamesUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.fetchOwnedGames(
            params.steamId,
            if (params.reload) CachePolicy.Remote else CachePolicy.CacheIfValid
        )
    }

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}