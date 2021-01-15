package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.test.rules.RoomTransactionsMockRule
import ru.luckycactus.steamroulette.util.TestData
import ru.luckycactus.steamroulette.util.TestData.testSteamId
import ru.luckycactus.steamroulette.util.fakes.NaiveGamesValidator

class LocalGamesDataSourceInsertBufferTest {

    @get:Rule
    val dbTransactionsMockRule = RoomTransactionsMockRule()

    @RelaxedMockK
    lateinit var dbMock: AppDatabase
    lateinit var localGamesDataSource: LocalGamesDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        localGamesDataSource = LocalGamesDataSource(dbMock, NaiveGamesValidator.Factory())
    }

    @Test
    fun `when update - should buffer games and insert them in db by chunks`() = runBlocking {
        val totalGames = 2000
        val manyGamesFlow = flow {
            for (id in 1..totalGames) {
                emit(TestData.ownedGameEntityDummy/*.copy(appId = id)*/)
            }
        }

        coEvery { dbMock.ownedGamesDao().getAllMetaData(any()) } returns emptyList()

        var gamesInserted = 0
        val gamesSlot = slot<List<OwnedGameRoomEntity>>()
        coEvery {
            dbMock.ownedGamesDao().insert(capture(gamesSlot))
        } answers {
            gamesInserted += gamesSlot.captured.size
            emptyList()
        }

        dbTransactionsMockRule.mockTransactions(dbMock)

        localGamesDataSource.update(testSteamId, manyGamesFlow)

        assertThat(gamesInserted).isEqualTo(totalGames)
        // insert at least 100 but not all games at once
        coVerify(atLeast = 2, atMost = totalGames / 100 + 1) {
            dbMock.ownedGamesDao().insert(any() as List<OwnedGameRoomEntity>)
        }
    }
}