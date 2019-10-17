package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.presentation.common.Event

class ObserveHiddenGamesClearUseCase(
    private val gamesRepository: GamesRepository
) : UseCase<SteamId, LiveData<Event<Unit>>>() {

    override fun getResult(params: SteamId): LiveData<Event<Unit>> {
        return object : MediatorLiveData<Event<Unit>>() {
            var lastHiddenCount: Int = 0
        }.apply {
            addSource(gamesRepository.observeHiddenGamesCount(params)) { newHiddenCount ->
                if (lastHiddenCount > 0 && newHiddenCount == 0)
                    value = Event(Unit)
                lastHiddenCount = newHiddenCount
            }
        }
    }
}