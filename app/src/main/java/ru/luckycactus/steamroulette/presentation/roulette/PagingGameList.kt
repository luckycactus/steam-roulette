package ru.luckycactus.steamroulette.presentation.roulette

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.presentation.common.Event

class PagingGameList @AssistedInject constructor(
    private val gamesRepository: GamesRepository,
    @Assisted private val steamId: SteamId,
    @Assisted private val gameIds: List<Int>,
    @Assisted private val minSize: Int,
    @Assisted private val fetchDistance: Int,
    @Assisted private val coroutineScope: CoroutineScope
) {
    val list: List<OwnedGame>
        get() = _list
    val itemsInsertedLiveData: LiveData<Event<Pair<Int, Int>>>
        get() = _itemsInsertedLiveData

    private val _list = mutableListOf<OwnedGame>()
    private val _itemsInsertedLiveData = MutableLiveData<Event<Pair<Int, Int>>>()
    private var nextFetchIndex = 0
    private var fetching = false
    private var fetchJob: Job? = null

    init {
        if (gameIds.isNotEmpty())
            fetch()
    }

    fun isEmpty() = gameIds.isEmpty()

    fun gamesEnded() = isEmpty() || (_list.isEmpty() && nextFetchIndex >= gameIds.size)

    @MainThread
    fun removeTop(): OwnedGame {
        val removedItem = _list.removeAt(0)
        if (_list.size <= minSize && !fetching && nextFetchIndex < gameIds.size)
            fetch()
        return removedItem
    }

    @MainThread
    fun finish() {
        fetchJob?.cancel()
    }

    @MainThread
    private fun fetch() {
        if (fetchJob?.isActive != true) {
            fetchJob = coroutineScope.launch(context = Dispatchers.Main) {
                val fetchEnd = minOf(nextFetchIndex + fetchDistance, gameIds.size)
                val games = gamesRepository.getLocalOwnedGames(
                    steamId,
                    gameIds.subList(
                        nextFetchIndex,
                        fetchEnd
                    )
                )
                if (isActive) {
                    nextFetchIndex = fetchEnd
                    _list.addAll(games)
                    _itemsInsertedLiveData.value = Event(_list.size - games.size to games.size)
                    fetching = false
                }
            }
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            steamId: SteamId,
            gameIds: List<Int>,
            minSize: Int = 3,
            fetchDistance: Int = 10
        ): PagingGameList
    }
}