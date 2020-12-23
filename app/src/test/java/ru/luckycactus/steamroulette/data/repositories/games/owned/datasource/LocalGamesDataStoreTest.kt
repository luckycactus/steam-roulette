package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.util.TestData
import ru.luckycactus.steamroulette.util.TestData.testSteamId
import ru.luckycactus.steamroulette.util.fakes.NaiveGamesVerifier

@RunWith(AndroidJUnit4::class)
class LocalGamesDataSourceTest {

    private lateinit var dataSource: LocalGamesDataSource
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
        dataSource = LocalGamesDataSource(db, NaiveGamesVerifier.Factory())
    }

    @After
    fun teardown() = runBlocking {
        db.ownedGamesDao().clear()
    }

    @Test
    fun `when update - while dataSource is empty - getAll should return inserted data`(): Unit =
        runBlocking {
            val data1 = listOf(
                OwnedGameEntity(1, "1"),
                OwnedGameEntity(2, "2"),
            )

            dataSource.update(testSteamId, data1.asFlow())
            val result = dataSource.getAll(testSteamId)

            assertThat(result).hasSameElementsAs(data1)
        }

    fun `when update - while dataSource contains data - should return new data`() =
        runBlocking {
            val data1 = listOf(
                OwnedGameEntity(1, "1"),
                OwnedGameEntity(2, "2"),
            )
            val data2 = listOf(
                OwnedGameEntity(1, "1", playtime2Weeks = 100),
                OwnedGameEntity(3, "3"),
            )
            dataSource.update(testSteamId, data1.asFlow())

            dataSource.update(testSteamId, data2.asFlow())
            val result = dataSource.getAll(testSteamId)

            assertThat(result).isEqualTo(data2)
        }

    @Test
    fun `when update - while dataSource contains games data - should preserve hidden and shown state for existing games`(): Unit =
        runBlocking {
            val data1 = listOf(
                OwnedGameEntity(1, "1"),
                OwnedGameEntity(2, "2"),
            )
            val data2 = listOf(
                OwnedGameEntity(1, "1"),
                OwnedGameEntity(3, "3"),
            )
            val ids1 = data1.map { it.appId }
            dataSource.update(testSteamId, data1.asFlow())
            dataSource.setHidden(testSteamId, ids1, true)
            dataSource.setShown(testSteamId, ids1, true)

            dataSource.update(testSteamId, data2.asFlow())
            val actualHiddenShownIds = db.ownedGamesDao().getIds(
                testSteamId.as64(),
                GamesFilter(shown = true, hidden = true),
                false
            ).sorted()

            assertThat(actualHiddenShownIds).isEqualTo(listOf(1))
        }

    @Test
    fun `when setHidden - getIds should filter games by hidden state`() =
        runBlocking {
            val data1 = listOf(
                OwnedGameEntity(1, "1"),
                OwnedGameEntity(2, "2"),
            )
            dataSource.update(testSteamId, data1.asFlow())
            val ids = data1.map { it.appId }

            val hiddenIds1 = dataSource.getIds(testSteamId, GamesFilter(hidden = true))
            dataSource.setHidden(testSteamId, ids.drop(1), true)
            val hiddenIds2 = dataSource.getIds(testSteamId, GamesFilter(hidden = true))
            val notHiddenIds2 = dataSource.getIds(testSteamId, GamesFilter(hidden = false))
            dataSource.setHidden(testSteamId, ids.drop(1), false)
            val hiddenIds3 = dataSource.getIds(testSteamId, GamesFilter(hidden = true))

            assertSoftly {
                it.assertThat(hiddenIds1).hasSameElementsAs(emptyList<Int>())
                it.assertThat(hiddenIds2).hasSameElementsAs(ids.drop(1))
                it.assertThat(notHiddenIds2).hasSameElementsAs(ids.take(1))
                it.assertThat(hiddenIds3).hasSameElementsAs(emptyList<Int>())
            }
        }

    @Test
    fun `when setShown - getIds should filter games by shown state`() =
        runBlocking {
            val data1 = listOf(
                OwnedGameEntity(1, "1"),
                OwnedGameEntity(2, "2"),
            )
            dataSource.update(testSteamId, data1.asFlow())
            val ids = data1.map { it.appId }

            val shownIds1 = dataSource.getIds(testSteamId, GamesFilter(shown = true))
            dataSource.setShown(testSteamId, ids.drop(1), true)
            val shownIds2 = dataSource.getIds(testSteamId, GamesFilter(shown = true))
            val notShownIds2 = dataSource.getIds(testSteamId, GamesFilter(shown = false))
            dataSource.setShown(testSteamId, ids.drop(1), false)
            val shownIds3 = dataSource.getIds(testSteamId, GamesFilter(shown = true))

            assertSoftly {
                it.assertThat(shownIds1).hasSameElementsAs(emptyList<Int>())
                it.assertThat(shownIds2).hasSameElementsAs(ids.drop(1))
                it.assertThat(notShownIds2).hasSameElementsAs(ids.take(1))
                it.assertThat(shownIds3).hasSameElementsAs(emptyList<Int>())
            }
        }


    @Test
    fun `when getIds - should return result filtered by playtime`() = runBlocking {
        val data1 = listOf(
            OwnedGameEntity(1, "1", playtimeForever = 1751),
            OwnedGameEntity(2, "2", playtimeForever = 0),
            OwnedGameEntity(3, "3", playtimeForever = 30),
        )
        dataSource.update(testSteamId, data1.asFlow())

        val allIdsActual = dataSource.getIds(
            testSteamId,
            GamesFilter(playtime = PlaytimeFilter.All)
        )
        val notPlayedIdsActual = dataSource.getIds(
            testSteamId,
            GamesFilter(playtime = PlaytimeFilter.NotPlayed)
        )
        val max1HourIdsActual = dataSource.getIds(
            testSteamId,
            GamesFilter(playtime = PlaytimeFilter.Limited(1))
        )

        val allIdsExpected = data1.map { it.appId }
        val notPlayedIdsExpected = data1.filter { it.playtimeForever == 0 }
            .map { it.appId }
        val max1HourIdsExpected = data1.filter { it.playtimeForever <= 60 }
            .map { it.appId }
        assertSoftly {
            it.assertThat(allIdsActual).hasSameElementsAs(allIdsExpected)
            it.assertThat(notPlayedIdsActual).hasSameElementsAs(notPlayedIdsExpected)
            it.assertThat(max1HourIdsActual).hasSameElementsAs(max1HourIdsExpected)
        }
    }
}