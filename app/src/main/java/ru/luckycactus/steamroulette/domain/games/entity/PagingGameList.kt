package ru.luckycactus.steamroulette.domain.games.entity

import android.util.SparseIntArray
import androidx.annotation.MainThread
import androidx.core.util.set
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe

interface PagingGameList {
    val list: List<GameHeader>
    val itemsInsertionsFlow: Flow<Pair<Int, Int>>
    val itemRemovalsFlow: Flow<Int>
    val topGameFlow: Flow<GameHeader?>

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
    coroutineScope: CoroutineScope
) : PagingGameList {

    override val list: List<GameHeader>
        get() = _list

    override val itemsInsertionsFlow by lazyNonThreadSafe { itemsInsertedChannel.asFlow() }
    override val itemRemovalsFlow by lazyNonThreadSafe { itemRemovedChannel.asFlow() }
    override val topGameFlow: Flow<GameHeader?>

    private val coroutineScope = coroutineScope + Job(coroutineScope.coroutineContext[Job])
    private val itemsInsertedChannel = BroadcastChannel<Pair<Int, Int>>(Channel.BUFFERED)
    private val itemRemovedChannel = BroadcastChannel<Int>(Channel.BUFFERED)

    private val _list = mutableListOf<GameHeader>()

    private var nextFetchIndex = 0
    private var fetching = false
    private val tmpIndicesMap = SparseIntArray(fetchDistance)
    private var state = State.NotStarted

    init {
        topGameFlow = combine(itemRemovalsFlow, itemsInsertionsFlow) { _, _ ->
            _list.firstOrNull()
        }.distinctUntilChanged().shareIn(coroutineScope, SharingStarted.WhileSubscribed())
    }

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
        itemRemovedChannel.offer(0)
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
        coroutineScope.cancel()
        itemRemovedChannel.close()
        itemsInsertedChannel.close()
        state = State.Closed
    }

    @MainThread
    private fun fetch() {
        coroutineScope.launch {
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
                .sortedBy { tmpIndicesMap[it.appId] }

            tmpIndicesMap.clear()
            if (isActive) {
                nextFetchIndex = fetchEnd
                _list.addAll(games)
                itemsInsertedChannel.offer(_list.size - games.size to games.size)
                fetching = false
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