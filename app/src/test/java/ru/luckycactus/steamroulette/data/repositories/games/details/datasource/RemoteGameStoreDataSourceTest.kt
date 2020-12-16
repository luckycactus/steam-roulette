package ru.luckycactus.steamroulette.data.repositories.games.details.datasource

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.data.net.api.SteamStoreApiService
import ru.luckycactus.steamroulette.di.common.MoshiModule
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException
import ru.luckycactus.steamroulette.util.fakes.FixedLanguageProvider
import ru.luckycactus.steamroulette.util.getJson
import ru.luckycactus.steamroulette.util.testCommonNetworkExceptions

class RemoteGameStoreDataSourceTest {

    @MockK
    private lateinit var service: SteamStoreApiService
    private lateinit var dataSource: RemoteGameStoreDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        dataSource = RemoteGameStoreDataSource(
            service,
            FixedLanguageProvider(),
            MoshiModule.provideMoshi()
        )
    }

    @Test
    fun `getGameStoreInfo() should successfully return data for game`() = runBlocking {
        val expectedId = 252950
        coEvery {
            service.getGamesStoreInfo(listOf(expectedId), any())
        } returns getJson("json/games/game_store_info_response_success.json").toResponseBody()
        val actualGame = dataSource.get(expectedId)
        assertEquals(expectedId, actualGame.appId)
    }

    @Test(expected = GetGameStoreInfoException::class)
    fun `getGameStoreInfo() should throw GetGameStoreInfoException if service returns error`() = runBlocking {
        coEvery {
            service.getGamesStoreInfo(listOf(961440), any())
        } returns getJson("json/games/game_store_info_response_error.json").toResponseBody()
        val data = dataSource.get(961440)
    }

    @Test
    fun `getGameStoreInfo() should throw correct common network exceptions`() = runBlockingTest {
        testCommonNetworkExceptions(
            { service.getGamesStoreInfo(any(), any()) },
            { dataSource.get(1) }
        )
    }
}