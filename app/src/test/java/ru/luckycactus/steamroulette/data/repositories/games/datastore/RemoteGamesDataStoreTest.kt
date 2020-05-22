package ru.luckycactus.steamroulette.data.repositories.games.datastore

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import ru.luckycactus.steamroulette.data.net.services.SteamApiService
import ru.luckycactus.steamroulette.data.net.services.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.di.common.MoshiModule
import ru.luckycactus.steamroulette.domain.common.GetGameStoreInfoException
import ru.luckycactus.steamroulette.domain.common.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.test.util.TestData
import ru.luckycactus.steamroulette.test.util.fakes.FixedLanguageProvider
import ru.luckycactus.steamroulette.test.util.getJson
import ru.luckycactus.steamroulette.test.util.testCommonNetworkExceptions


class RemoteGamesDataStoreTest {

    private lateinit var steamApiServiceMock: SteamApiService
    private lateinit var steamStoreApiServiceMock: SteamStoreApiService
    private lateinit var remoteGamesDataStore: RemoteGamesDataStore

    @Before
    fun setup() {
        steamApiServiceMock = mock(SteamApiService::class.java)
        steamStoreApiServiceMock = mock(SteamStoreApiService::class.java)
        remoteGamesDataStore = RemoteGamesDataStore(
            steamApiServiceMock,
            steamStoreApiServiceMock,
            MoshiModule.provideMoshiForApi(),
            FixedLanguageProvider()
        )
    }

    @Test
    fun `getOwnedGames() should return correct data`() = runBlocking {
        given(steamApiServiceMock.getOwnedGames(any(), any(), any()))
            .willReturn(getJson("json/games/game_store_info_response_success.json").toResponseBody())
        val dataFlow = remoteGamesDataStore.getOwnedGames(TestData.gabenSteamId)
        val actualData = dataFlow.toList()
        assertEquals(TestData.ownedGamesData, actualData)
    }

    @Test(expected = GetOwnedGamesPrivacyException::class)
    fun `getOwnedGames() should throw GetOwnedGamesPrivacyException if games array is missing`() =
        runBlocking {
            given(steamApiServiceMock.getOwnedGames(any(), any(), any()))
                .willReturn(getJson("json/games/owned_games_response_empty.json").toResponseBody())
            remoteGamesDataStore.getOwnedGames(TestData.gabenSteamId).collect()
        }

    @Test
    fun `getOwnedGames() should throw correct common network exceptions`() = runBlockingTest {
        testCommonNetworkExceptions(steamApiServiceMock.getOwnedGames(any(), any(), any())) {
            remoteGamesDataStore.getOwnedGames(TestData.gabenSteamId)
        }
    }

    @Test
    fun `getOwnedGames() should return empty result if no games`() = runBlocking {
        given(steamApiServiceMock.getOwnedGames(any(), any(), any()))
            .willReturn(
                getJson("json/games/owned_games_response_no_games.json").toResponseBody()
            )
        val data = remoteGamesDataStore.getOwnedGames(TestData.gabenSteamId).toList()
        assertEquals(emptyList<OwnedGameEntity>(), data)
    }

    @Test
    fun `getGameStoreInfo() should successfully return data for game`() = runBlocking {
        given(steamStoreApiServiceMock.getGamesStoreInfo(eq(listOf(252950)), any()))
            .willReturn(getJson("json/games/game_store_info_response_success.json").toResponseBody())
        val data = remoteGamesDataStore.getGameStoreInfo(252950)
    }

    @Test(expected = GetGameStoreInfoException::class)
    fun `getGameStoreInfo() should throw GetGameStoreInfoException`() = runBlocking {
        given(steamStoreApiServiceMock.getGamesStoreInfo(eq(listOf(961440)), any()))
            .willReturn(getJson("json/games/game_store_info_response_error.json").toResponseBody())
        val data = remoteGamesDataStore.getGameStoreInfo(961440)
    }

    @Test
    fun `getGameStoreInfo() should throw correct common network exceptions`() = runBlockingTest {
        testCommonNetworkExceptions(steamStoreApiServiceMock.getGamesStoreInfo(any(), any())) {
            remoteGamesDataStore.getGameStoreInfo(1)
        }
    }
}