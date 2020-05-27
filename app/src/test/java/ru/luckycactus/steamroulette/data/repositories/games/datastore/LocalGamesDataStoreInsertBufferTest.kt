package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.room.withTransaction
import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.test.util.TestData
import ru.luckycactus.steamroulette.test.util.TestData.gabenSteamId
import ru.luckycactus.steamroulette.test.util.fakes.NaiveGamesVerifier

class LocalGamesDataStoreInsertBufferTest {

    private lateinit var dbMock: AppDatabase
    private lateinit var localGamesDataStore: LocalGamesDataStore

    @Before
    fun setup() {
        dbMock = mockk(relaxed = true)
        localGamesDataStore = LocalGamesDataStore(dbMock, NaiveGamesVerifier.Factory())

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
        coEvery { dbMock.ownedGamesDao().getAllIds(any()) } returns emptyList()
        coEvery { dbMock.ownedGamesDao().getHiddenIds(any()) } returns emptyList()
        coEvery { dbMock.ownedGamesDao().getShownIds(any()) } returns emptyList()
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

        localGamesDataStore.updateOwnedGames(gabenSteamId, manyGamesFlow)

        assertEquals(totalGames, gamesInserted)
        coVerify(atLeast = 2, atMost = 10) {
            dbMock.ownedGamesDao().insert(any() as List<OwnedGameRoomEntity>)
        }
    }

}