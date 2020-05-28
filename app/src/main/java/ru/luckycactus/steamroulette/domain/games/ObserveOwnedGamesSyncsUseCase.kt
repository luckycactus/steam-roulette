package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import javax.inject.Inject

@Reusable
class ObserveOwnedGamesSyncsUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : AbstractUseCase<ObserveOwnedGamesSyncsUseCase.Params, Flow<Long>>() {

    override fun execute(params: Params): Flow<Long> {
        return gamesRepository.observeGamesUpdates(params.steamId)
    }

    data class Params(
        val steamId: SteamId
    )
}