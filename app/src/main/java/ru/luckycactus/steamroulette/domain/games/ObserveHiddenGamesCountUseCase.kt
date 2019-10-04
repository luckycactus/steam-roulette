package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class ObserveHiddenGamesCountUseCase(
    private val gamesRepository: GamesRepository
) : UseCase<SteamId, LiveData<Int>>() {

    override fun getResult(params: SteamId): LiveData<Int> {
        return gamesRepository.observeHiddenGamesCount(params)
    }
}