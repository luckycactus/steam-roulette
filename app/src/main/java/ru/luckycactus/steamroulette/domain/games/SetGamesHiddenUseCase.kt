package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import javax.inject.Inject

@Reusable
class SetGamesHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetGamesHiddenUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.setLocalOwnedGamesHidden(params.steamId, params.gameIds, params.hide)
    }

    data class Params(
        val steamId: SteamId,
        val gameIds: List<Int>,
        val hide: Boolean
    )
}