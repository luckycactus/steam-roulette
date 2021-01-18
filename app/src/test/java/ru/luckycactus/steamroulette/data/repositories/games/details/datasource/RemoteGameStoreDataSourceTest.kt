package ru.luckycactus.steamroulette.data.repositories.games.details.datasource

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.data.net.api.SteamStoreApiService
import ru.luckycactus.steamroulette.di.common.MoshiModule
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException
import ru.luckycactus.steamroulette.util.fakes.FixedLanguageProvider
import ru.luckycactus.steamroulette.util.getJson

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
    fun `when service returns success response for game request - get() should return game object`(): Unit =
        runBlocking {
            val expectedId = 252950
            coEvery {
                service.getGamesStoreInfo(listOf(expectedId), any())
            } returns getJson("json/games/game_store_info_response_success.json").toResponseBody()
            val actualGame = dataSource.get(expectedId)
            assertThat(actualGame.appId).isEqualTo(expectedId)
        }

    @Test(expected = GetGameStoreInfoException::class)
    fun `when service returns error response - get() should throw GetGameStoreInfoException`() =
        runBlocking {
            coEvery {
                service.getGamesStoreInfo(listOf(961440), any())
            } returns getJson("json/games/game_store_info_response_error.json").toResponseBody()
            val data = dataSource.get(961440)
        }
}