package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import javax.inject.Inject

@Reusable
class ObserveHiddenGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : AbstractUseCase<SteamId, Flow<Int>>() {

    override fun execute(params: SteamId): Flow<Int> {
        return gamesRepository.observeHiddenGamesCount(params)
    }
}