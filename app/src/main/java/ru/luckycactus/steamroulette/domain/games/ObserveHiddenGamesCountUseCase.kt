package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import javax.inject.Inject

@Reusable
class ObserveHiddenGamesCountUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<SteamId, LiveData<Int>>() {

    override fun getResult(params: SteamId): LiveData<Int> {
        return gamesRepository.observeHiddenGamesCount(params)
    }
}