package ru.luckycactus.steamroulette.data.repositories.games.datasource

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.data.net.services.SteamApiService
import ru.luckycactus.steamroulette.data.net.services.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.di.common.MoshiModule
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.test.util.TestData
import ru.luckycactus.steamroulette.test.util.fakes.FixedLanguageProvider
import ru.luckycactus.steamroulette.test.util.getJson
import ru.luckycactus.steamroulette.test.util.testCommonNetworkExceptions


class RemoteGamesDataSourceTest {

    private lateinit var steamApiServiceMock: SteamApiService
    private lateinit var steamStoreApiServiceMock: SteamStoreApiService
    private lateinit var remoteGamesDataSource: RemoteGamesDataSource

    @Before
    fun setup() {
        steamApiServiceMock = mockk()
        steamStoreApiServiceMock = mockk()
        remoteGamesDataSource = RemoteGamesDataSource(
            steamApiServiceMock,
            steamStoreApiServiceMock,
            MoshiModule.provideMoshiForApi(),
            FixedLanguageProvider()
        )
    }

    @Test
    fun `getOwnedGames() should return correct data`() = runBlocking {
        coEvery {
            steamApiServiceMock.getOwnedGames(any(), any(), any())
        } returns getJson("json/games/owned_games_response_success.json").toResponseBody()
        val dataFlow = remoteGamesDataSource.getOwnedGames(TestData.gabenSteamId)
        val actualData = dataFlow.toList()
        assertEquals(TestData.ownedGamesData, actualData)
    }

    @Test(expected = GetOwnedGamesPrivacyException::class)
    fun `getOwnedGames() should throw GetOwnedGamesPrivacyException if games array is missing`() =
        runBlocking {
            coEvery {
                steamApiServiceMock.getOwnedGames(any(), any(), any())
            } returns getJson("json/games/owned_games_response_empty.json").toResponseBody()
            remoteGamesDataSource.getOwnedGames(TestData.gabenSteamId).collect()
        }

    @Test
    fun `getOwnedGames() should throw correct common network exceptions`() = runBlockingTest {
        testCommonNetworkExceptions(
            { steamApiServiceMock.getOwnedGames(any(), any(), any()) },
            { remoteGamesDataSource.getOwnedGames(TestData.gabenSteamId) }
        )
    }

    @Test
    fun `getOwnedGames() should return empty result if no games`() = runBlocking {
        coEvery {
            steamApiServiceMock.getOwnedGames(any(), any(), any())
        } returns getJson("json/games/owned_games_response_no_games.json").toResponseBody()

        val data = remoteGamesDataSource.getOwnedGames(TestData.gabenSteamId).toList()
        assertEquals(emptyList<OwnedGameEntity>(), data)
    }

    @Test
    fun `getGameStoreInfo() should successfully return data for game`() = runBlocking {
        coEvery {
            steamStoreApiServiceMock.getGamesStoreInfo(listOf(252950), any())
        } returns getJson("json/games/game_store_info_response_success.json").toResponseBody()
        val data = remoteGamesDataSource.getGameStoreInfo(252950)
    }

    @Test(expected = GetGameStoreInfoException::class)
    fun `getGameStoreInfo() should throw GetGameStoreInfoException`() = runBlocking {
        coEvery {
            steamStoreApiServiceMock.getGamesStoreInfo(listOf(961440), any())
        } returns getJson("json/games/game_store_info_response_error.json").toResponseBody()
        val data = remoteGamesDataSource.getGameStoreInfo(961440)
    }

    @Test
    fun `getGameStoreInfo() should throw correct common network exceptions`() = runBlockingTest {
        testCommonNetworkExceptions(
            { steamStoreApiServiceMock.getGamesStoreInfo(any(), any()) },
            { remoteGamesDataSource.getGameStoreInfo(1) }
        )
    }
}