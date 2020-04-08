package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.UseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class ObserveOwnedGamesSyncsUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<ObserveOwnedGamesSyncsUseCase.Params, LiveData<Long>>() {

    override fun getResult(params: Params): LiveData<Long> {
        return gamesRepository.observeGamesUpdates(params.steamId)
    }

    data class Params(
        val steamId: SteamId
    )
}