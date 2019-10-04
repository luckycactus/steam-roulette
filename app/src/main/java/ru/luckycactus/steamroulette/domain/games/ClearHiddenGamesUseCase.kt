package ru.luckycactus.steamroulette.domain.games

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class ClearHiddenGamesUseCase(
    private val gamesRepository: GamesRepository
): SuspendUseCase<SteamId, Unit>() {

    override suspend fun getResult(params: SteamId) {
        gamesRepository.clearHiddenGames(params)
    }
}