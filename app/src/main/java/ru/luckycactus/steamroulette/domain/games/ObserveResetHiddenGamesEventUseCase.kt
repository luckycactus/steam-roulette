package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.UseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.Event
import javax.inject.Inject

@Reusable
class ObserveResetHiddenGamesEventUseCase @Inject constructor(
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