package ru.luckycactus.steamroulette.domain.games

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Assert.fail
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
    fun `when invoke - firstShownGameId should split ids list onto not shown ids and shown ids`() =
        testScope.runBlockingTest {
            initWithDefaultIds()
            lastTopGameId = shownIds.last()
            setupMockForSuccessResult()

            val (pagingList, firstShownGameId) = runUseCaseAndRequireSuccess()

            val ids = pagingList.ids.drop(1) // first element contains last top game
            val i = ids.indexOf(firstShownGameId)
            assertSoftly {
                it.assertThat(i).isGreaterThan(0)
                it.assertThat(ids.take(i)).hasSameElementsAs(notShownIds)
                it.assertThat(ids.drop(i) + lastTopGameId).hasSameElementsAs(shownIds)
            }
        }

    @Test
    fun `when invoke - ids should be shuffled`() = runBlocking {
        initWithDefaultIds()
        lastTopGameId = 0
        setupMockForSuccessResult()

        val (pagingList, firstShownGameId) = runUseCaseAndRequireSuccess()

        val i = pagingList.ids.indexOf(firstShownGameId)
        val notShownIdsSubList = pagingList.ids.take(i)
        val shownIdsSubList = pagingList.ids.drop(i)

        // there is tiny chance of shuffled list being equal to original one,
        // but chance of it is so negligible that we can ignore it
        assertSoftly {
            it.assertThat(notShownIdsSubList).isNotEqualTo(notShownIds)
            it.assertThat(shownIdsSubList).isNotEqualTo(shownIds)
        }
    }

    @Test
    fun `when invoke - while lastTopGameId is containing in games list - ids list should start with lastTopGameId`() =
        runBlocking {
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
    fun `when invoke - while lastTopGameId is absent in games list - ids list should not contain lastTopGameId`(): Unit =
        runBlocking {
            runBlocking {
                initWithDefaultIds()
                lastTopGameId = 100
                setupMockForSuccessResult()

                val (pagingList, _) = runUseCaseAndRequireSuccess()
                assertThat(pagingList.ids).doesNotContain(lastTopGameId)
            }
        }

    @Test
    fun `when invoke - while user has no games - should return failure`() =
        testScope.runBlockingTest {
            coEvery { gamesRepoMock.isUserHasGames() } returns false

            val result = getOwnedGamesPagingListUseCase.invoke(
                GetOwnedGamesPagingListUseCase.Params(
                    GamesFilter.all(),
                    testScope
                )
            )

            assertThat(result).isInstanceOf(GetOwnedGamesPagingListUseCase.Result.Fail.NoOwnedGames::class.java)
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

    private suspend fun runUseCaseAndRequireSuccess() =
        getOwnedGamesPagingListUseCase.invoke(
            GetOwnedGamesPagingListUseCase.Params(
                GamesFilter.all(),
                testScope
            )
        ) as GetOwnedGamesPagingListUseCase.Result.Success
}