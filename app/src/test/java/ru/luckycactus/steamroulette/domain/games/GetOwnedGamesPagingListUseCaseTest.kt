package ru.luckycactus.steamroulette.domain.games

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.Is
import org.hamcrest.core.IsInstanceOf
import org.junit.Test
import org.junit.Assert.*
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.test.util.TestData

class GetOwnedGamesPagingListUseCaseTest {

    private val testScope = TestCoroutineScope()

    @Test
    fun overall() = testScope.runBlockingTest {
        val shownIds = listOf(1, 3, 5)
        val notShownIds = listOf(2, 4, 6)

        val gamesRepositoryMock = mockk<GamesRepository>()
        coEvery { gamesRepositoryMock.isUserHasGames(any()) } returns true
        coEvery {
            gamesRepositoryMock.getVisibleLocalOwnedGamesIds(any(), any(), true)
        } returns shownIds
        coEvery {
            gamesRepositoryMock.getVisibleLocalOwnedGamesIds(any(), any(), false)
        } returns notShownIds

        val getOwnedGamesPagingListUseCase = GetOwnedGamesPagingListUseCase(gamesRepositoryMock)
        val result = getOwnedGamesPagingListUseCase.invoke(
            GetOwnedGamesPagingListUseCase.Params(
                TestData.gabenSteamId,
                PlaytimeFilter.All,
                testScope
            )
        ) as GetOwnedGamesPagingListUseCase.Result.Success

        assertEquals((shownIds + notShownIds).sorted(), result.pagingList.gameIds.sorted())
        assertTrue(shownIds.contains(result.firstShownGameId))

        val i = result.pagingList.gameIds.indexOf(result.firstShownGameId)
        assertEquals(notShownIds, result.pagingList.gameIds.take(i).sorted())
        assertEquals(shownIds, result.pagingList.gameIds.drop(i).sorted())
    }

    @Test
    fun `no games`() = testScope.runBlockingTest {
        val gamesRepositoryMock = mockk<GamesRepository>()
        coEvery { gamesRepositoryMock.isUserHasGames(any()) } returns false

        val getOwnedGamesPagingListUseCase = GetOwnedGamesPagingListUseCase(gamesRepositoryMock)

        assertThat(
            getOwnedGamesPagingListUseCase.invoke(
                GetOwnedGamesPagingListUseCase.Params(
                    TestData.gabenSteamId,
                    PlaytimeFilter.All,
                    testScope
                )
            ),
            IsInstanceOf(GetOwnedGamesPagingListUseCase.Result.Fail.NoOwnedGames::class.java)
        )
    }
}