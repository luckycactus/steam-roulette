package ru.luckycactus.steamroulette.data.repositories.games.datasource

import androidx.room.withTransaction
import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.test.util.TestData
import ru.luckycactus.steamroulette.test.util.TestData.gabenSteamId
import ru.luckycactus.steamroulette.test.util.fakes.NaiveGamesVerifier

class LocalGamesDataSourceInsertBufferTest {

    private lateinit var dbMock: AppDatabase
    private lateinit var localGamesDataSource: LocalGamesDataSource

    @Before
    fun setup() {
        dbMock = mockk(relaxed = true)
        localGamesDataSource = LocalGamesDataSource(dbMock, NaiveGamesVerifier.Factory())

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(
            "androidx.room.RoomDatabaseKt"
        )
    }

    @Test
    fun `updateOwnedGames() should buffer games and insert them by chunks`() = runBlocking {
        val totalGames = 2000
        coEvery { dbMock.ownedGamesDao().getIds(any()) } returns emptyList()
        coEvery { dbMock.ownedGamesDao().getIds(any(), hidden = true) } returns emptyList()
        coEvery { dbMock.ownedGamesDao().getIds(any(), shown = true) } returns emptyList()
        val gamesSlot = slot<List<OwnedGameRoomEntity>>()
        var gamesInserted = 0
        coEvery {
            dbMock.ownedGamesDao().insert(capture(gamesSlot))
        } answers {
            gamesInserted += gamesSlot.captured.size
            emptyList()
        }

        val manyGamesFlow = flow {
            for (id in 1..totalGames) {
                emit(TestData.ownedGameEntityDummy/*.copy(appId = id)*/)
            }
        }

        coEvery { dbMock.withTransaction(captureLambda<suspend () -> Any>()) } coAnswers {
            lambda<suspend () -> Any>().captured.invoke()
        }

        localGamesDataSource.updateOwnedGames(gabenSteamId, manyGamesFlow)

        assertEquals(totalGames, gamesInserted)
        coVerify(atLeast = 2, atMost = 10) {
            dbMock.ownedGamesDao().insert(any() as List<OwnedGameRoomEntity>)
        }
    }

}