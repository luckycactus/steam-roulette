package ru.luckycactus.steamroulette.domain.games.entity

import android.util.SparseIntArray
import androidx.annotation.MainThread
import androidx.core.util.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
    fun start()

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

    init {
        check(minSize > 0) { "minSize should be greater than 0" }
        check(fetchDistance > 0) { "fetchDistance should be greater than 0" }
    }

    override val list: List<GameHeader>
        get() = _list
    override val itemsInsertedChannel: ReceiveChannel<Pair<Int, Int>>
        get() = _itemsInsertedChannel
    override val itemRemovedChannel: ReceiveChannel<Int>
        get() = _itemRemovedChannel

    private val _list = mutableListOf<GameHeader>()
    private val _itemsInsertedChannel = Channel<Pair<Int, Int>>(Channel.BUFFERED)
    private val _itemRemovedChannel = Channel<Int>(Channel.BUFFERED)

    private var nextFetchIndex = 0
    private var fetching = false
    private var fetchJob: Job? = null
    private val tmpIndicesMap = SparseIntArray(fetchDistance)
    private var state = State.NotStarted

    override fun start() {
        checkState(State.NotStarted)
        state = State.Started
        if (gameIds.isNotEmpty())
            fetch()
    }

    override fun isEmpty() = gameIds.isEmpty()

    override fun isFinished() = isEmpty() || (_list.isEmpty() && nextFetchIndex >= gameIds.size)

    @MainThread
    override fun removeTop(): GameHeader {
        checkState(State.Started)
        val removedItem = _list.removeAt(0)
        _itemRemovedChannel.offer(0)
        if (_list.size <= minSize && !fetching && nextFetchIndex < gameIds.size)
            fetch()
        return removedItem
    }

    @MainThread
    override fun peekTop(): GameHeader? {
        checkState(State.Started)
        return _list.firstOrNull()
    }

    @MainThread
    override fun close() {
        checkState(State.NotStarted, State.Started)
        fetchJob?.cancel()
        state = State.Closed
    }

    @MainThread
    private fun fetch() {
        if (fetchJob?.isActive != true) {
            fetchJob = coroutineScope.launch {
                //if first fetch and fetchDistance <= minSize
                val fetchDistance = if (nextFetchIndex == 0 && fetchDistance <= minSize)
                    minSize + 1
                else
                    fetchDistance
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

    private fun checkState(vararg permittedStates: State) {
        if (state !in permittedStates) {
            val message = when (state) {
                State.NotStarted -> "$this is not started yet"
                State.Started -> "$this is already started"
                State.Closed -> "$this is already closed"
            }
            throw IllegalStateException(message)
        }
    }

    private enum class State {
        NotStarted,
        Started,
        Closed
    }
}