package ru.luckycactus.steamroulette.data.repositories.games.datasource

import androidx.collection.arrayMapOf
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import dagger.Reusable
import ru.luckycactus.steamroulette.data.core.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.data.net.services.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoResult
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RemoteGameDetailsDataSource @Inject constructor(
    private val steamStoreApiService: SteamStoreApiService,
    private val languageProvider: LanguageProvider,
    @Named("api") private val moshi: Moshi
) : GameDetailsDataSource.Remote {

    private val gameStoreInfoResultAdapter =
        moshi.adapter(GameStoreInfoResult::class.java)

    //todo document
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getGameStoreInfo(appId: Int): GameStoreInfoEntity {
        val response = wrapCommonNetworkExceptions {
            steamStoreApiService.getGamesStoreInfo(
                listOf(appId),
                languageProvider.getLanguageForStoreApi()
            )
        }

        val reader = JsonReader.of(response.source())
        val results = arrayMapOf<String, GameStoreInfoResult>()
        try {
            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                val obj = gameStoreInfoResultAdapter.fromJson(reader)!!
                results[name] = obj
            }
        } finally {
            reader.close()
            response.close()
        }

        val result = results[appId.toString()] ?: throw GetGameStoreInfoException()
        if (!result.success || result.gameStoreInfo == null)
            throw GetGameStoreInfoException()
        return result.gameStoreInfo
    }
}