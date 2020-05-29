package ru.luckycactus.steamroulette.data.repositories.games.datastore

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.test.util.TestData
import ru.luckycactus.steamroulette.test.util.TestData.gabenSteamId
import ru.luckycactus.steamroulette.test.util.fakes.NaiveGamesVerifier

@RunWith(AndroidJUnit4::class)
class LocalGamesDataStoreTest {

    private lateinit var localGamesDataStore: LocalGamesDataStore
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
        localGamesDataStore = LocalGamesDataStore(db, NaiveGamesVerifier.Factory())
    }

    @After
    fun tearDown() = runBlocking {
        db.ownedGamesDao().clear()
    }


    @Test
    fun `updateOwnedGames() should simply insert games into empty table and then return them`() =
        runBlocking {
            localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesData.asFlow())
            val actualData = localGamesDataStore.getOwnedGames(gabenSteamId).sortedBy { it.appId }
            val expectedData = TestData.ownedGamesData.sortedBy { it.appId }
            assertEquals(expectedData, actualData)
        }

    @Test
    fun `test games hiding`() = runBlocking {
        localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesData.asFlow())
        val ids = TestData.ownedGamesData.map { it.appId }.sorted()

        assertEquals(ids, getAllVisibleSortedIds())

        localGamesDataStore.setOwnedGamesHidden(gabenSteamId, ids.drop(1), true)
        assertEquals(ids.subList(0, 1), getAllVisibleSortedIds())

        localGamesDataStore.setOwnedGamesHidden(gabenSteamId, ids.drop(1), false)
        assertEquals(ids, getAllVisibleSortedIds())
    }

    @Test
    fun `updateOwnedGames() should preserve existing games hidden state`() = runBlocking {
        localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesData.asFlow())
        val ids = TestData.ownedGamesData.map { it.appId }.sorted()

        localGamesDataStore.setOwnedGamesHidden(gabenSteamId, ids.take(1), true)
        localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesDataUpdated.asFlow())

        val actualData = localGamesDataStore.getOwnedGames(gabenSteamId).sortedBy { it.appId }
        val expectedData = TestData.ownedGamesDataUpdated.sortedBy { it.appId }
        assertEquals(expectedData, actualData)
        assertEquals(ids.drop(1), getAllVisibleSortedIds())
    }

    @Test
    fun `updateOwnedGames() should preserve existing games shown state`() = runBlocking {
        localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesData.asFlow())
        val ids = TestData.ownedGamesData.map { it.appId }.sorted()

        localGamesDataStore.setOwnedGamesShown(gabenSteamId, ids.take(1), true)
        localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesDataUpdated.asFlow())

        val actualData = localGamesDataStore.getOwnedGames(gabenSteamId).sortedBy { it.appId }
        val expectedData = TestData.ownedGamesDataUpdated.sortedBy { it.appId }
        assertEquals(expectedData, actualData)
        assertEquals(ids.take(1), getAllVisibleSortedIds(shown = true))
    }

    private suspend fun getAllVisibleSortedIds(shown: Boolean = false) =
        db.ownedGamesDao().getVisibleIds(gabenSteamId.as64(), shown).sorted()


    @Test
    fun `getVisibleOwnedGamesIds() should properly filter output by playtime`() = runBlocking {
        localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesData.asFlow())

        assertEquals(
            TestData.ownedGamesData.map { it.appId }.sorted(),
            localGamesDataStore.getVisibleOwnedGamesIds(
                gabenSteamId,
                PlaytimeFilter.All,
                shown = false
            ).sorted()
        )

        assertEquals(
            TestData.ownedGamesData.asSequence()
                .filter { it.playtimeForever == 0 }
                .map { it.appId }
                .sorted()
                .toList(),
            localGamesDataStore.getVisibleOwnedGamesIds(
                gabenSteamId,
                PlaytimeFilter.NotPlayed,
                shown = false
            ).sorted()
        )

        assertEquals(
            TestData.ownedGamesData.asSequence()
                .filter { it.playtimeForever <= 60 }
                .map { it.appId }
                .sorted()
                .toList(),
            localGamesDataStore.getVisibleOwnedGamesIds(
                gabenSteamId,
                PlaytimeFilter.Limited(1),
                shown = false
            )
        )
    }

    @Test
    fun `getVisibleOwnedGamesIds() should properly filter output by shown state`() = runBlocking {
        localGamesDataStore.updateOwnedGames(gabenSteamId, TestData.ownedGamesData.asFlow())

        val filteredIds = TestData.ownedGamesData.asSequence()
            .filter { it.playtimeForever < 60 }
            .map { it.appId }
            .sorted()
            .toList()

        assertEquals(
            filteredIds,
            localGamesDataStore.getVisibleOwnedGamesIds(
                gabenSteamId,
                PlaytimeFilter.Limited(1),
                shown = false
            ).sorted()
        )

        assertEquals(
            emptyList<Int>(),
            localGamesDataStore.getVisibleOwnedGamesIds(
                gabenSteamId,
                PlaytimeFilter.Limited(1),
                shown = true
            ).sorted()
        )

        localGamesDataStore.setOwnedGamesShown(gabenSteamId, filteredIds.take(1), true)

        assertEquals(
            filteredIds.drop(1),
            localGamesDataStore.getVisibleOwnedGamesIds(
                gabenSteamId,
                PlaytimeFilter.Limited(1),
                shown = false
            ).sorted()
        )

        assertEquals(
            filteredIds.take(1),
            localGamesDataStore.getVisibleOwnedGamesIds(
                gabenSteamId,
                PlaytimeFilter.Limited(1),
                shown = true
            ).sorted()
        )
    }

}