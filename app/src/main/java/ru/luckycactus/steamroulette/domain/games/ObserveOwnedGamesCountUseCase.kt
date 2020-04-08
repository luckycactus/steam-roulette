package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.UseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class ObserveOwnedGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
): UseCase<ObserveOwnedGamesCountUseCase.Params, LiveData<Int>>() {

    override fun getResult(params: Params): LiveData<Int> {
        return gamesRepository.observeGamesCount(params.steamId)
    }

    data class Params(
        val steamId: SteamId
    )
}