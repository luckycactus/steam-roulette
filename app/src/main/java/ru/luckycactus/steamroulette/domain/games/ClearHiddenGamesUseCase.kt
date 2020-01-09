package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class ClearHiddenGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
): SuspendUseCase<SteamId, Unit>() {

    override suspend fun getResult(params: SteamId) {
        gamesRepository.resetHiddenGames(params)
    }
}