package ru.luckycactus.steamroulette.domain.games

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter

class GetOwnedGamesPagingListUseCaseTest {

    val testScope = TestCoroutineScope()

    @MockK
    lateinit var gamesRepoMock: GamesRepository

    @MockK
    lateinit var rouletteRepoMock: RouletteRepository

    lateinit var getOwnedGamesPagingListUseCase: GetOwnedGamesPagingListUseCase

    lateinit var shownIds: List<Int>
    lateinit var notShownIds: List<Int>
    var lastTopGameId: Int = 0

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        getOwnedGamesPagingListUseCase = GetOwnedGamesPagingListUseCase(
            gamesRepoMock,
            rouletteRepoMock
        )
    }

    @Test
    fun `firstShownGameId should split ids list onto not shown ids and shown ids`() =
        testScope.runBlockingTest {
            initWithDefaultIds()
            lastTopGameId = shownIds.last()
            setupMockForSuccessResult()

            val (pagingList, firstShownGameId) = runUseCaseAndRequireSuccess()
            val ids = pagingList.ids.drop(1) // first element contains last top game
            val i = ids.indexOf(firstShownGameId)
            assertTrue(i >= 0)
            assertEquals(
                notShownIds,
                ids.take(i).sorted()
            )
            assertEquals(
                shownIds,
                (ids.drop(i) + lastTopGameId).sorted()
            )
        }

    @Test
    fun `ids should be shuffled`() = runBlocking {
        initWithDefaultIds()
        lastTopGameId = 0
        setupMockForSuccessResult()

        val (pagingList, firstShownGameId) = runUseCaseAndRequireSuccess()

        val i = pagingList.ids.indexOf(firstShownGameId)
        val notShownIdsSubList = pagingList.ids.take(i)
        val shownIdsSubList = pagingList.ids.drop(i)

        // there is tiny chance of shuffled list being equal to original one,
        // but chance of it is so negligible that we can ignore it
        assertNotEquals(notShownIds, notShownIdsSubList)
        assertNotEquals(shownIds, shownIdsSubList)
    }

    @Test
    fun `ids list should start with last top game id`() = runBlocking {
        initWithDefaultIds()
        lastTopGameId = shownIds.last()
        setupMockForSuccessResult()

        repeat(5) {
            val (pagingList, _) = runUseCaseAndRequireSuccess()
            if (pagingList.ids.first() != lastTopGameId)
                fail()
        }
    }

    @Test
    fun `ids list should not contain last top game id if it is absent in initial lists`() =
        runBlocking {
            initWithDefaultIds()
            lastTopGameId = 100
            setupMockForSuccessResult()

            val (pagingList, _) = runUseCaseAndRequireSuccess()
            assertFalse(pagingList.ids.contains(lastTopGameId))
        }

    @Test
    fun `no games`() = testScope.runBlockingTest {
        coEvery { gamesRepoMock.isUserHasGames() } returns false

        assertThat(
            getOwnedGamesPagingListUseCase.invoke(
                GetOwnedGamesPagingListUseCase.Params(
                    GamesFilter.all(),
                    testScope
                )
            ),
            IsInstanceOf(GetOwnedGamesPagingListUseCase.Result.Fail.NoOwnedGames::class.java)
        )
    }

    private fun initWithDefaultIds() {
        shownIds = generateSequence(1) { it + 2 }.take(10).toList()
        notShownIds = generateSequence(2) { it + 2 }.take(10).toList()
    }

    private fun setupMockForSuccessResult() {
        coEvery { gamesRepoMock.isUserHasGames() } returns true

        val gamesFilterSlot = slot<GamesFilter>()
        coEvery {
            gamesRepoMock.getOwnedGamesIdsMutable(gamesFilter = capture(gamesFilterSlot), any())
        } answers {
            if (gamesFilterSlot.captured.shown == true)
                shownIds.toMutableList()
            else notShownIds.toMutableList()
        }

        coEvery { rouletteRepoMock.getLastTopGameId() } returns lastTopGameId
    }

    private suspend fun runUseCaseAndRequireSuccess() = getOwnedGamesPagingListUseCase.invoke(
        GetOwnedGamesPagingListUseCase.Params(
            GamesFilter.all(),
            testScope
        )
    ) as GetOwnedGamesPagingListUseCase.Result.Success
}