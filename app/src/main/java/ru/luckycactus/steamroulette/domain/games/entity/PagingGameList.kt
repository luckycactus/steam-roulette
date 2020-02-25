package ru.luckycactus.steamroulette.domain.games.entity

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import ru.luckycactus.steamroulette.domain.core.Event

interface PagingGameList {
    val list: List<GameHeader>
    val itemsInsertedLiveData: LiveData<Event<Pair<Int, Int>>>
    fun isEmpty(): Boolean
    fun gamesEnded(): Boolean
    @MainThread
    fun removeTop(): GameHeader
    @MainThread
    fun finish()
}

class PagingGameListImpl constructor(
    private val gamesFactory: suspend (List<Int>) -> List<GameHeader>,
    private val gameIds: List<Int>,
    private val minSize: Int,
    private val fetchDistance: Int,
    private val coroutineScope: CoroutineScope
) : PagingGameList {

    override val list: List<GameHeader>
        get() = _list
    override val itemsInsertedLiveData: LiveData<Event<Pair<Int, Int>>>
        get() = _itemsInsertedLiveData

    private val _list = mutableListOf<GameHeader>()
    private val _itemsInsertedLiveData = MutableLiveData<Event<Pair<Int, Int>>>()
    private var nextFetchIndex = 0
    private var fetching = false
    private var fetchJob: Job? = null

    init {
        if (gameIds.isNotEmpty())
            fetch()
    }

    override fun isEmpty() = gameIds.isEmpty()

    override fun gamesEnded() = isEmpty() || (_list.isEmpty() && nextFetchIndex >= gameIds.size)

    @MainThread
    override fun removeTop(): GameHeader {
        val removedItem = _list.removeAt(0)
        if (_list.size <= minSize && !fetching && nextFetchIndex < gameIds.size)
            fetch()
        return removedItem
    }

    @MainThread
    override fun finish() {
        fetchJob?.cancel()
    }

    @MainThread
    private fun fetch() {
        if (fetchJob?.isActive != true) {
            fetchJob = coroutineScope.launch(context = Dispatchers.Main) {
                val fetchEnd = minOf(nextFetchIndex + fetchDistance, gameIds.size)
                val games = gamesFactory(
                    gameIds.subList(
                        nextFetchIndex,
                        fetchEnd
                    )
                )
                if (isActive) {
                    nextFetchIndex = fetchEnd
                    _list.addAll(games)
                    _itemsInsertedLiveData.value =
                        Event(_list.size - games.size to games.size)
                    fetching = false
                }
            }
        }
    }
}