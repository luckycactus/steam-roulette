package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.util.TestData
import ru.luckycactus.steamroulette.util.TestData.testSteamId
import ru.luckycactus.steamroulette.util.fakes.NaiveGamesVerifier

@RunWith(AndroidJUnit4::class)
class LocalGamesDataSourceTest {

    private lateinit var localGamesDataSource: LocalGamesDataSource
    private val db: AppDatabase

    init {
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .build()
        runBlocking {
            db.userSummaryDao().insert(TestData.userSummaryEntityDummy)
        }
    }

    @Before
    fun setup() {
        localGamesDataSource = LocalGamesDataSource(db, NaiveGamesVerifier.Factory())
    }

    @After
    fun tearDown() = runBlocking {
        db.ownedGamesDao().clear()
    }


    @Test
    fun `updateOwnedGames() should simply insert games into empty table and then return them`() =
        runBlocking {
            localGamesDataSource.update(testSteamId, TestData.ownedGamesData.asFlow())
            val actualData = localGamesDataSource.getAll(testSteamId).sortedBy { it.appId }
            val expectedData = TestData.ownedGamesData.sortedBy { it.appId }
            assertEquals(expectedData, actualData)
        }

    @Test
    fun `test games hiding`() = runBlocking {
        localGamesDataSource.update(testSteamId, TestData.ownedGamesData.asFlow())
        val ids = TestData.ownedGamesData.map { it.appId }.sorted()

        assertEquals(ids, getAllVisibleSortedIds())

        localGamesDataSource.setHidden(testSteamId, ids.drop(1), true)
        assertEquals(ids.subList(0, 1), getAllVisibleSortedIds())

        localGamesDataSource.setHidden(testSteamId, ids.drop(1), false)
        assertEquals(ids, getAllVisibleSortedIds())
    }

    @Test
    fun `updateOwnedGames() should preserve existing games hidden state`() = runBlocking {
        localGamesDataSource.update(testSteamId, TestData.ownedGamesData.asFlow())
        val ids = TestData.ownedGamesData.map { it.appId }.sorted()

        localGamesDataSource.setHidden(testSteamId, ids.take(1), true)
        localGamesDataSource.update(testSteamId, TestData.ownedGamesDataUpdated.asFlow())

        val actualData = localGamesDataSource.getAll(testSteamId).sortedBy { it.appId }
        val expectedData = TestData.ownedGamesDataUpdated.sortedBy { it.appId }
        assertEquals(expectedData, actualData)
        assertEquals(ids.drop(1), getAllVisibleSortedIds())
    }

    @Test
    fun `updateOwnedGames() should preserve existing games shown state`() = runBlocking {
        localGamesDataSource.update(testSteamId, TestData.ownedGamesData.asFlow())
        val ids = TestData.ownedGamesData.map { it.appId }.sorted()

        localGamesDataSource.setShown(testSteamId, ids.take(1), true)
        localGamesDataSource.update(testSteamId, TestData.ownedGamesDataUpdated.asFlow())

        val actualData = localGamesDataSource.getAll(testSteamId).sortedBy { it.appId }
        val expectedData = TestData.ownedGamesDataUpdated.sortedBy { it.appId }
        assertEquals(expectedData, actualData)
        assertEquals(ids.take(1), getAllVisibleSortedIds(shown = true))
    }

    private suspend fun getAllVisibleSortedIds(shown: Boolean = false) =
        db.ownedGamesDao().getIds(
            testSteamId.as64(),
            GamesFilter(shown = shown, hidden = false),
            false
        ).sorted()

    @Test
    fun `getVisibleOwnedGamesIds() should properly filter output by playtime`() = runBlocking {
        localGamesDataSource.update(testSteamId, TestData.ownedGamesData.asFlow())

        assertEquals(
            TestData.ownedGamesData.map { it.appId }.sorted(),
            localGamesDataSource.getIds(
                testSteamId,
                GamesFilter(shown = false, hidden = false, playtime = PlaytimeFilter.All)
            ).sorted()
        )

        assertEquals(
            TestData.ownedGamesData.asSequence()
                .filter { it.playtimeForever == 0 }
                .map { it.appId }
                .sorted()
                .toList(),
            localGamesDataSource.getIds(
                testSteamId,
                GamesFilter(shown = false, hidden = false, playtime = PlaytimeFilter.NotPlayed)
            ).sorted()
        )

        assertEquals(
            TestData.ownedGamesData.asSequence()
                .filter { it.playtimeForever <= 60 }
                .map { it.appId }
                .sorted()
                .toList(),
            localGamesDataSource.getIds(
                testSteamId,
                GamesFilter(shown = false, hidden = false, playtime = PlaytimeFilter.Limited(1))
            ).sorted()
        )
    }

    @Test
    fun `getVisibleOwnedGamesIds() should properly filter output by shown state`() = runBlocking {
        localGamesDataSource.update(testSteamId, TestData.ownedGamesData.asFlow())

        val filteredIds = TestData.ownedGamesData.asSequence()
            .filter { it.playtimeForever < 60 }
            .map { it.appId }
            .sorted()
            .toList()

        assertEquals(
            filteredIds,
            localGamesDataSource.getIds(
                testSteamId,
                GamesFilter(shown = false, hidden = false, playtime = PlaytimeFilter.Limited(1))
            ).sorted()
        )

        assertEquals(
            emptyList<Int>(),
            localGamesDataSource.getIds(
                testSteamId,
                GamesFilter(shown = true, hidden = false, playtime = PlaytimeFilter.Limited(1))
            ).sorted()
        )

        localGamesDataSource.setShown(testSteamId, filteredIds.take(1), true)

        assertEquals(
            filteredIds.drop(1),
            localGamesDataSource.getIds(
                testSteamId,
                GamesFilter(shown = false, hidden = false, playtime = PlaytimeFilter.Limited(1))
            ).sorted()
        )

        assertEquals(
            filteredIds.take(1),
            localGamesDataSource.getIds(
                testSteamId,
                GamesFilter(shown = true, hidden = false, playtime = PlaytimeFilter.Limited(1))
            ).sorted()
        )
    }

}