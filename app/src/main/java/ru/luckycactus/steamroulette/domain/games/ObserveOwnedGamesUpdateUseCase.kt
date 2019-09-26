package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import java.util.*

class ObserveOwnedGamesUpdateUseCase(
    private val gamesRepository: GamesRepository
) : UseCase<ObserveOwnedGamesUpdateUseCase.Params, LiveData<Date>>() {

    override fun getResult(params: Params): LiveData<Date> {
        return gamesRepository.observeGamesUpdates(params.steamId)
    }

    data class Params(
        val steamId: SteamId
    )
}