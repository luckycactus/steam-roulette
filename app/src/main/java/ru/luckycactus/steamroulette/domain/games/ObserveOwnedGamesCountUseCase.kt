package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class ObserveOwnedGamesCountUseCase(
    private val gamesRepository: GamesRepository
): UseCase<ObserveOwnedGamesCountUseCase.Params, LiveData<Int>>() {

    override fun getResult(params: Params): LiveData<Int> {
        return gamesRepository.observeGamesCount(params.steamId)
    }

    data class Params(
        val steamId: SteamId
    )
}