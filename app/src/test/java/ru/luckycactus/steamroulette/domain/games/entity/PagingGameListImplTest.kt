package ru.luckycactus.steamroulette.domain.games.entity

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.internal.toImmutableList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PagingGameListImplTest {

    private lateinit var testScope: TestCoroutineScope
    private lateinit var pagingList: PagingGameList

    @Before
    fun setup() {
        testScope = TestCoroutineScope()
    }

    @After
    fun teardown() {
        testScope.coroutineContext.cancel()
        testScope.cleanupTestCoroutines()
        pagingList.close()
    }

    @Test
    fun `when isFinished -while not started and not empty - should be false`() {
        initPagingList()

        assertThat(pagingList.isFinished()).isFalse()
    }

    @Test
    fun `when isFinished - while not started and empty - should be true`() {
        initPagingList(ids = emptyList())

        assertThat(pagingList.isFinished()).isTrue()
    }

    @Test
    fun `when isFinished - while all items removed - should be true`() {
        initPagingList()
        pagingList.start()

        repeat(pagingList.ids.size) {
            pagingList.removeTop()
        }

        assertThat(pagingList.isFinished()).isTrue()
    }

    @Test
    fun `when peekTop and removeTop - while not started - should throw exception`() {
        initPagingList()

        assertThatThrownBy { pagingList.peekTop() }
        assertThatThrownBy { pagingList.removeTop() }
    }

    @Test
    fun `when peekTop - while finished - should return null`() {
        initPagingList(ids = emptyList())
        pagingList.start()

        assertThat(pagingList.peekTop()).isNull()
    }

    @Test
    fun `when removeTop - while finished - should throw Exception`() {
        initPagingList(ids = emptyList())

        assertThatThrownBy { pagingList.removeTop() }
    }

    @Test
    fun `when start - while already started - should throw exception`() {
        initPagingList()

        pagingList.start()

        assertThatThrownBy { pagingList.start() }
    }

    @Test
    fun `when peekTop and removeTop - should return same items`() {
        initPagingList()
        pagingList.start()

        while (!pagingList.isFinished()) {
            assertThat(pagingList.peekTop()).isEqualTo(pagingList.removeTop())
        }
    }

    @Test
    fun `topGameFlow should emit top items followed by null in the end`() =
        testScope.runBlockingTest {
            val items = listOf(1, 2, 3)
            initPagingList(ids = items)

            val topGames = async { pagingList.topGameFlow.take(items.size + 1).toList() }
            pagingList.start()

            removeTopUntilFinished()

            assertThat(topGames.await().map { it?.appId }).isEqualTo(items + null)
        }

    @Test
    fun `itemsInsertionsFlow and itemRemovalsFlow should conform list changes`() {
        val ids = (1..10).toList()
        initPagingList(ids = ids)

        var currentIndex = 0
        val expectedIds = mutableListOf<Int>()
        val expectedLists = mutableListOf<List<Int>>()
        val actualLists = mutableListOf<List<Int>>()

        testScope.launch {
            pagingList.itemsInsertionsFlow.collect {
                expectedIds.addAll(it.first, ids.subList(currentIndex, currentIndex + it.second))
                currentIndex += it.second
                expectedLists += expectedIds.toImmutableList()
                actualLists += pagingList.list.map { it.appId }
            }
        }

        testScope.launch {
            pagingList.itemRemovalsFlow.collect {
                expectedIds.removeAt(it)
                expectedLists += expectedIds.toImmutableList()
                actualLists += pagingList.list.map { it.appId }
            }
        }

        pagingList.start()

        removeTopUntilFinished()

        assertThat(actualLists).isNotEmpty().hasSameElementsAs(expectedLists)
    }

    @Test
    fun `while empty - flows should be terminated`() {
        initPagingList(ids = emptyList())

        val itemsRemovalsJob = testScope.launch {
            pagingList.itemRemovalsFlow.collect { }
        }
        val itemsInsertionsJob = testScope.launch {
            pagingList.itemRemovalsFlow.collect { }
        }

        pagingList.start()

        assertSoftly {
            it.assertThat(itemsRemovalsJob.isCompleted).isTrue
            it.assertThat(itemsInsertionsJob.isCompleted).isTrue
        }
    }

    @Test
    fun `while finished - flows should be terminated`() {
        initPagingList()

        val itemsRemovalsJob = testScope.launch {
            pagingList.itemRemovalsFlow.collect { }
        }
        val itemsInsertionsJob = testScope.launch {
            pagingList.itemRemovalsFlow.collect { }
        }

        pagingList.start()

        removeTopUntilFinished()

        assertSoftly {
            it.assertThat(itemsRemovalsJob.isCompleted).isTrue
            it.assertThat(itemsInsertionsJob.isCompleted).isTrue
        }
    }

    @Test
    fun `while closed - flows should be terminated`() {
        initPagingList()

        val itemsRemovalsJob = testScope.launch {
            pagingList.itemRemovalsFlow.collect { }
        }
        val itemsInsertionsJob = testScope.launch {
            pagingList.itemRemovalsFlow.collect { }
        }
        pagingList.start()
        pagingList.close()

        assertSoftly {
            it.assertThat(itemsRemovalsJob.isCompleted).isTrue()
            it.assertThat(itemsInsertionsJob.isCompleted).isTrue()
        }
    }

    @Test
    fun `while after close - flows should be empty`() = testScope.runBlockingTest {
        initPagingList()
        pagingList.start()
        pagingList.close()

        val itemRemovals = pagingList.itemRemovalsFlow.toList()
        val itemInsertions = pagingList.itemsInsertionsFlow.toList()
        val topGames = mutableListOf<GameHeader?>()
        testScope.launch { pagingList.topGameFlow.collect { topGames.add(it) } }
        testScope.advanceUntilIdle()

        assertSoftly {
            it.assertThat(itemRemovals).isNotNull().isEmpty()
            it.assertThat(itemInsertions).isNotNull().isEmpty()
            it.assertThat(topGames).isNotNull().isEmpty()
        }
    }

    @Test
    fun `while fetchDistance less than or equal to minSize - should fetch more than minSize items on first time`() {
        val minSize = 3
        initPagingList(ids = (1..10).toList(), minSize = minSize, fetchDistance = 2)
        pagingList.start()

        val size = pagingList.list.size

        assertThat(size).isGreaterThan(minSize)
    }

    @Test
    fun `should insert items in the amount of fetchDistance items`() {
        val minSize = 1
        val fetchDistance = 2
        initPagingList(minSize = minSize, fetchDistance = fetchDistance)

        val insertions = mutableListOf<Int>()
        testScope.launch {
            pagingList.itemsInsertionsFlow.collect {
                insertions.add(it.second)
            }
        }
        pagingList.start()

        removeTopUntilFinished()

        assertSoftly {
            it.assertThat(insertions.dropLast(1)).isNotEmpty().allMatch { it == fetchDistance }
            it.assertThat(insertions.last()).isLessThanOrEqualTo(fetchDistance)
        }
    }

    @Test
    fun `while list size == minSize - should insert new items`() {
        val minSize = 1
        val fetchDistance = 2
        initPagingList(minSize = minSize, fetchDistance = fetchDistance)

        val listSizesWhenInsert = mutableListOf<Int>()

        testScope.launch {
            pagingList.itemsInsertionsFlow.collect {
                val count = it.second
                listSizesWhenInsert += (pagingList.list.size - count)
            }
        }
        pagingList.start()

        removeTopUntilFinished()

        assertThat(listSizesWhenInsert.drop(1)).isNotEmpty().allMatch { it == minSize }
    }

    private fun initPagingList(
        ids: List<Int> = listOf(1, 2, 3),
        minSize: Int = 2,
        fetchDistance: Int = 3
    ) {
        pagingList = PagingGameListImpl(
            { _ids -> _ids.map { GameHeader(it, it.toString()) } },
            ids,
            minSize,
            fetchDistance,
            testScope
        )
    }

    private fun removeTopUntilFinished() {
        while (!pagingList.isFinished()) {
            pagingList.removeTop()
        }
    }
}