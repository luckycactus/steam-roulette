package ru.luckycactus.steamroulette.data.repositories.games.details.datasource

import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import ru.luckycactus.steamroulette.data.net.api.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.details.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.data.repositories.games.details.models.GameStoreInfoResult
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoException
import javax.inject.Inject

class RemoteGameStoreDataSource @Inject constructor(
    private val steamStoreApiService: SteamStoreApiService,
    private val languageProvider: LanguageProvider,
    moshi: Moshi
) : GameStoreDataSource.Remote {

    private val gameStoreInfoResultAdapter =
        moshi.adapter(GameStoreInfoResult::class.java)

    //todo document
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun get(appId: Int): GameStoreInfoEntity {
        val response = steamStoreApiService.getGamesStoreInfo(
            listOf(appId),
            languageProvider.getLanguageForStoreApi()
        )

        val results = parseResults(response)

        val result = results[appId.toString()] ?: throw GetGameStoreInfoException()
        if (!result.success || result.gameStoreInfo == null)
            throw GetGameStoreInfoException()
        return result.gameStoreInfo
    }

    private fun parseResults(response: ResponseBody): ArrayMap<String, GameStoreInfoResult> {
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
        return results
    }
}