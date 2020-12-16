package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.data.net.api.SteamApiService
import ru.luckycactus.steamroulette.data.net.api.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.di.common.MoshiModule
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.util.TestData
import ru.luckycactus.steamroulette.util.getJson
import ru.luckycactus.steamroulette.util.testCommonNetworkExceptions


class RemoteGamesDataSourceTest {

    @MockK
    lateinit var steamApiServiceMock: SteamApiService

    @MockK
    lateinit var steamStoreApiServiceMock: SteamStoreApiService

    lateinit var remoteGamesDataSource: RemoteGamesDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        remoteGamesDataSource = RemoteGamesDataSource(
            steamApiServiceMock,
            MoshiModule.provideMoshi()
        )
    }

    @Test
    fun `getOwnedGames() should return correct data`() = runBlocking {
        coEvery {
            steamApiServiceMock.getOwnedGames(any(), any(), any())
        } returns getJson("json/games/owned_games_response_success.json").toResponseBody()

        val actual = remoteGamesDataSource.getAll(TestData.testSteamId).toList()

        assertEquals(TestData.ownedGamesData, actual)
    }

    @Test(expected = GetOwnedGamesPrivacyException::class)
    fun `getOwnedGames() should throw GetOwnedGamesPrivacyException if games array is missing`() =
        runBlocking {
            coEvery {
                steamApiServiceMock.getOwnedGames(any(), any(), any())
            } returns getJson("json/games/owned_games_response_empty.json").toResponseBody()

            remoteGamesDataSource.getAll(TestData.testSteamId).collect()
        }

    @Test
    fun `getOwnedGames() should throw correct common network exceptions`() = runBlockingTest {
        testCommonNetworkExceptions(
            { steamApiServiceMock.getOwnedGames(any(), any(), any()) },
            { remoteGamesDataSource.getAll(TestData.testSteamId) }
        )
    }

    @Test
    fun `getOwnedGames() should return empty result if no games`() = runBlocking {
        coEvery {
            steamApiServiceMock.getOwnedGames(any(), any(), any())
        } returns getJson("json/games/owned_games_response_no_games.json").toResponseBody()

        val data = remoteGamesDataSource.getAll(TestData.testSteamId).toList()
        assertEquals(emptyList<OwnedGameEntity>(), data)
    }
}