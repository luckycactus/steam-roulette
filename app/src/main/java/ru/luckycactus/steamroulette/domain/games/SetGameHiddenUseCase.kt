package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class SetGameHiddenUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<SetGameHiddenUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        gamesRepository.setLocalOwnedGameHidden(params.steamId, params.gameId, params.hide)
    }

    data class Params(
        val steamId: SteamId,
        val gameId: Int,
        val hide: Boolean
    )
}