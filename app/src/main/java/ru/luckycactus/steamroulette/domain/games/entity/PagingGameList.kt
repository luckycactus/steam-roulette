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
    val ids: List<Int>
    val data: List<GameHeader>
    val itemsInsertionsFlow: Flow<Pair<Int, Int>>
    val itemRemovalsFlow: Flow<Int>
    val topGameFlow: Flow<GameHeader?>
    val coroutineScope: CoroutineScope

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
    override val ids: List<Int>,
    private val minSize: Int,
    private val fetchDistance: Int,
    parentScope: CoroutineScope
) : PagingGameList {

    override val data: List<GameHeader>
        get() = _list

    override val itemsInsertionsFlow by lazyNonThreadSafe { itemsInsertedChannel.asFlow() }
    override val itemRemovalsFlow by lazyNonThreadSafe { itemRemovedChannel.asFlow() }
    override val topGameFlow: Flow<GameHeader?>

    override val coroutineScope = parentScope + Job(parentScope.coroutineContext[Job])
    private val itemsInsertedChannel = BroadcastChannel<Pair<Int, Int>>(Channel.BUFFERED)
    private val itemRemovedChannel = BroadcastChannel<Int>(Channel.BUFFERED)

    private val _list = mutableListOf<GameHeader>()

    private var nextFetchIndex = 0
    private var fetching = false
    private val tmpIndicesMap = SparseIntArray(fetchDistance)
    private var state = State.NotStarted

    init {
        topGameFlow = merge(
            itemRemovalsFlow.map { },
            itemsInsertionsFlow.map { }
        ).map { _list.firstOrNull() }
            .distinctUntilChanged()
            .shareIn(coroutineScope, SharingStarted.Eagerly)

        if (isFinished())
            closeChannels()
    }

    override fun start() {
        checkState(State.NotStarted)
        state = State.Started
        if (ids.isNotEmpty())
            fetch()
    }

    override fun isEmpty() = ids.isEmpty()

    override fun isFinished() = _list.isEmpty() && nextFetchIndex >= ids.size

    @MainThread
    override fun removeTop(): GameHeader {
        checkState(State.Started)
        check(_list.isNotEmpty())

        val removedItem = _list.removeAt(0)
        itemRemovedChannel.offer(0)
        if (_list.size <= minSize && !fetching && nextFetchIndex < ids.size)
            fetch()
        if (isFinished())
            closeChannels()
        return removedItem
    }

    @MainThread
    override fun peekTop(): GameHeader? {
        checkState(State.Started)
        return _list.firstOrNull()
    }

    @MainThread
    override fun close() {
        coroutineScope.cancel()
        closeChannels()
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
            val fetchEnd = minOf(nextFetchIndex + fetchDistance, ids.size)
            val fetchIds = ids.subList(
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

    private fun closeChannels() {
        itemRemovedChannel.close()
        itemsInsertedChannel.close()
    }

    private enum class State {
        NotStarted,
        Started,
        Closed
    }
}