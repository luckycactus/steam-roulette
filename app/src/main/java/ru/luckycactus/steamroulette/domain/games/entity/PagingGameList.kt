package ru.luckycactus.steamroulette.domain.games.entity

import android.util.SparseIntArray
import androidx.annotation.MainThread
import androidx.core.util.set
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

interface PagingGameList {
    val list: List<GameHeader>
    val itemsInsertedChannel: ReceiveChannel<Pair<Int, Int>>
    val itemRemovedChannel: ReceiveChannel<Int>
    fun isEmpty(): Boolean
    fun isFinished(): Boolean

    @MainThread
    fun removeTop(): GameHeader
    fun peekTop(): GameHeader?

    @MainThread
    fun close()
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
    override val itemsInsertedChannel: ReceiveChannel<Pair<Int, Int>>
        get() = _itemsInsertedChannel
    override val itemRemovedChannel: ReceiveChannel<Int>
        get() = _itemRemovedChannel

    private val _list = mutableListOf<GameHeader>()
    private val _itemsInsertedChannel = Channel<Pair<Int, Int>>()
    private val _itemRemovedChannel = Channel<Int>(0)

    private var nextFetchIndex = 0
    private var fetching = false
    private var fetchJob: Job? = null
    private val tmpIndicesMap = SparseIntArray(fetchDistance)

    init {
        if (gameIds.isNotEmpty())
            fetch()
    }

    override fun isEmpty() = gameIds.isEmpty()

    override fun isFinished() = isEmpty() || (_list.isEmpty() && nextFetchIndex >= gameIds.size)

    @MainThread
    override fun removeTop(): GameHeader {
        val removedItem = _list.removeAt(0)
        if (_list.size <= minSize && !fetching && nextFetchIndex < gameIds.size)
            fetch()
        _itemRemovedChannel.offer(0)
        return removedItem
    }

    @MainThread
    override fun peekTop(): GameHeader? = _list.firstOrNull()

    @MainThread
    override fun close() {
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
                    tmpIndicesMap[i] = index
                }
                val games = gamesFactory(fetchIds)
                    .sortedWith(Comparator { o1, o2 ->
                        tmpIndicesMap[o1.appId] - tmpIndicesMap[o2.appId]
                    })
                tmpIndicesMap.clear()
                if (isActive) {
                    nextFetchIndex = fetchEnd
                    _list.addAll(games)
                    _itemsInsertedChannel.send(_list.size - games.size to games.size)
                    fetching = false
                }
            }
        }
    }
}