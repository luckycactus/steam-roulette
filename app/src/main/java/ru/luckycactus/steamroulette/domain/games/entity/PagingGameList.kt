package ru.luckycactus.steamroulette.domain.games.entity

import android.util.SparseIntArray
import androidx.annotation.MainThread
import androidx.core.util.set
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import ru.luckycactus.steamroulette.domain.core.Event

interface PagingGameList {
    val list: List<GameHeader>
    val itemsInsertedLiveData: LiveData<Event<Pair<Int, Int>>>
    fun isEmpty(): Boolean
    fun allGamesShowed(): Boolean
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
    private val tmpIndiciesMap = SparseIntArray(fetchDistance)

    init {
        if (gameIds.isNotEmpty())
            fetch()
    }

    override fun isEmpty() = gameIds.isEmpty()

    override fun allGamesShowed() = isEmpty() || (_list.isEmpty() && nextFetchIndex >= gameIds.size)

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
                val fetchIds = gameIds.subList(
                    nextFetchIndex,
                    fetchEnd
                )
                fetchIds.forEachIndexed { index, i ->
                    tmpIndiciesMap[i] = index
                }
                val games = gamesFactory(fetchIds)
                    .sortedWith(Comparator { o1, o2 ->
                        tmpIndiciesMap[o1.appId] - tmpIndiciesMap[o2.appId]
                    })
                tmpIndiciesMap.clear()
                if (isActive) {
                    nextFetchIndex = fetchEnd
                    _list.addAll(games)
                    _itemsInsertedLiveData.value = Event(_list.size - games.size to games.size)
                    fetching = false
                }
            }
        }
    }
}