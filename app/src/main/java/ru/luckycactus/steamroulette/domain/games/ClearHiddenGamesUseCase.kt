package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import javax.inject.Inject

@Reusable
class ClearHiddenGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
): AbstractSuspendUseCase<SteamId, Unit>() {

    override suspend fun execute(params: SteamId) {
        gamesRepository.resetHiddenGames(params)
    }
}