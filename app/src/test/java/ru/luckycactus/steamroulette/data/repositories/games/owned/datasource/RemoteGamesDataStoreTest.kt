package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.data.net.api.SteamApiService
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.di.common.MoshiModule
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.util.TestData
import ru.luckycactus.steamroulette.util.getJson


class RemoteGamesDataSourceTest {

    @MockK
    lateinit var steamApiServiceMock: SteamApiService

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
    fun `when getAll - while service returns success response - should return parsed games`(): Unit =
        runBlocking {
            coEvery {
                steamApiServiceMock.getOwnedGames(any(), any(), any())
            } returns getJson("json/games/owned_games_response_success.json").toResponseBody()

            val actual = remoteGamesDataSource.getAll(TestData.testSteamId).toList()

            assertThat(actual).isEqualTo(TestData.ownedGamesData)
        }

    @Test(expected = GetOwnedGamesPrivacyException::class)
    fun `when getAll - while service returns empty response - should throw GetOwnedGamesPrivacyException`() =
        runBlocking {
            coEvery {
                steamApiServiceMock.getOwnedGames(any(), any(), any())
            } returns getJson("json/games/owned_games_response_empty.json").toResponseBody()

            remoteGamesDataSource.getAll(TestData.testSteamId).collect()
        }

    @Test
    fun `when getAll - while service returns success response with empty games array - should return empty result`(): Unit =
        runBlocking {
            coEvery {
                steamApiServiceMock.getOwnedGames(any(), any(), any())
            } returns getJson("json/games/owned_games_response_no_games.json").toResponseBody()

            val data = remoteGamesDataSource.getAll(TestData.testSteamId).toList()
            assertThat(data).isEqualTo(emptyList<OwnedGameEntity>())
        }
}