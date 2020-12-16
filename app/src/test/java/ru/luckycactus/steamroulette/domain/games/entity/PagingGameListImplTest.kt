package ru.luckycactus.steamroulette.domain.games.entity

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PagingGameListImplTest {

    private lateinit var testScope: TestCoroutineScope

    @Before
    fun setup() {
        testScope = TestCoroutineScope()
    }

    @After
    fun tearDown() {
        testScope.coroutineContext.cancel()
    }

    @Test
    fun `test overall`() = testScope.runBlockingTest {
        val ids = (1..10).toList()
        val pagingGameList = PagingGameListImpl(
            { _ids -> _ids.map { GameHeader(it, "") } },
            ids,
            2,
            3,
            testScope
        )
        var currentIndex = 0
        val expectedIds = mutableListOf<Int>()

        testScope.launch {
            pagingGameList.itemsInsertionsFlow.collect {
                expectedIds.addAll(it.first, ids.subList(currentIndex, currentIndex + it.second))
                currentIndex += it.second
                assertEquals(expectedIds, pagingGameList.list.map { it.appId })
            }
        }

        testScope.launch {
            pagingGameList.itemRemovalsFlow.collect {
                expectedIds.removeAt(it)
                assertEquals(expectedIds, pagingGameList.list.map { it.appId })
            }
        }

        assertFalse(pagingGameList.isEmpty())
        assertFalse(pagingGameList.isFinished())

        pagingGameList.start()

        while (!pagingGameList.isFinished()) {
            assertEquals(pagingGameList.peekTop(), pagingGameList.removeTop())
        }

        assertEquals(currentIndex, ids.size)
        assertFalse(pagingGameList.isEmpty())
        assertTrue(pagingGameList.isFinished())
        assertNull(pagingGameList.peekTop())
    }

    @Test
    fun `should fetch more than minSize items on first time if fetchDistance less than or equal to minSize`() {
        val minSize = 3
        val fetchDistance = 2
        val pagingGameList = PagingGameListImpl(
            { it.map { GameHeader(it, "") } },
            (1..10).toList(),
            minSize,
            fetchDistance,
            testScope
        )
        pagingGameList.start()
        val size = pagingGameList.list.size
        assertTrue(size > minSize)
    }

    @Test
    fun `test fetchDistance`() {
        val minSize = 1
        val fetchDistance = 2
        val pagingGameList = PagingGameListImpl(
            { it.map { GameHeader(it, "") } },
            (1..10).toList(),
            minSize,
            fetchDistance,
            testScope
        )

        testScope.launch {
            pagingGameList.itemsInsertionsFlow.collect {
                val (index, count) = it
                if (index + count < 10) {
                    assertEquals(fetchDistance, count)
                } else {
                    assertTrue(count <= fetchDistance)
                }
            }
        }

        pagingGameList.start()

        while (!pagingGameList.isFinished()) {
            pagingGameList.removeTop()
        }
    }

    @Test
    fun `test minSize`() {
        val minSize = 1
        val fetchDistance = 2
        val pagingGameList = PagingGameListImpl(
            { it.map { GameHeader(it, "") } },
            (1..10).toList(),
            minSize,
            fetchDistance,
            testScope
        )

        testScope.launch {
            pagingGameList.itemsInsertionsFlow.collect {
                val (_, count) = it
                if (pagingGameList.list.size - count > 0) {
                    //not initial fetch
                    assertEquals(pagingGameList.list.size - count, minSize)
                }
            }
        }

        pagingGameList.start()

        while (!pagingGameList.isFinished()) {
            pagingGameList.removeTop()
        }
    }

    @Test
    fun `test empty`() {
        val pagingGameList = PagingGameListImpl(
            { mockk() },
            emptyList(),
            3,
            3,
            testScope
        )
        assertTrue(pagingGameList.isEmpty())
        assertTrue(pagingGameList.isFinished())
    }

    @Test
    fun `test closing`() {
        val request =
            spyk<suspend (List<Int>) -> List<GameHeader>>({ it.map { GameHeader(it, "") } })
        val factory = spyk<suspend (List<Int>) -> List<GameHeader>>({
            delay(1000)
            request(it)
        })
        val pagingGameList = PagingGameListImpl(
            factory,
            (1..10).toList(),
            2,
            3,
            testScope
        )
        pagingGameList.start()
        testScope.advanceUntilIdle()
        assertEquals(3, pagingGameList.list.size)

        pagingGameList.removeTop()
        testScope.advanceTimeBy(500)

        coVerify(exactly = 2) {
            factory.invoke(any())
        }
        coVerify(exactly = 1) {
            request.invoke(any())
        }
    }
}