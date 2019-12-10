package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import javax.inject.Inject


//todo lm remove
@Reusable
@Deprecated("")
class GetAllLocalOwnedGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetAllLocalOwnedGamesUseCase.Params, List<OwnedGame>>() {

    data class Params(
        val steamId: SteamId,
        val filter: EnPlayTimeFilter
    )

    override suspend fun getResult(params: Params): List<OwnedGame> {
        return gamesRepository.getFilteredLocalOwnedGames(params.steamId, params.filter).shuffled()
    }
}