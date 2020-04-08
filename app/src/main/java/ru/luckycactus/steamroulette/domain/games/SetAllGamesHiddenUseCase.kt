package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import javax.inject.Inject

@Reusable
class SetAllGamesHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetAllGamesHiddenUseCase.Params, Unit>() {
    override suspend fun getResult(params: Params) {
        gamesRepository.setAllLocalOwnedGamesHidden(params.steamId, params.hide)
    }

    data class Params(
        val steamId: SteamId,
        val hide: Boolean
    )
}