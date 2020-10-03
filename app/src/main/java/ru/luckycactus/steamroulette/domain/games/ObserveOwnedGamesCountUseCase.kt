package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import javax.inject.Inject

@Reusable
class ObserveOwnedGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<GamesFilter, Flow<Int>>() {

    override fun execute(params: GamesFilter): Flow<Int> {
        return gamesRepository.observeGamesCount(params)
    }
}