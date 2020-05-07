package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.UseCase
import javax.inject.Inject

@Reusable
class ObserveHiddenGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<SteamId, Flow<Int>>() {

    override fun getResult(params: SteamId): Flow<Int> {
        return gamesRepository.observeHiddenGamesCount(params)
    }
}