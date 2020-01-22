package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.collection.arrayMapOf
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.net.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoResult
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.data.utils.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.domain.exception.GetGameStoreInfoException
import ru.luckycactus.steamroulette.domain.exception.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RemoteGamesDataStore @Inject constructor(
    private val steamApiService: SteamApiService,
    private val steamStoreApiService: SteamStoreApiService,
    @Named("api") private val gson: Gson
) : GamesDataStore.Remote {

    override suspend fun getOwnedGames(steam64: Long): Flow<OwnedGameEntity> {
        val response = wrapCommonNetworkExceptions {
            steamApiService.getOwnedGames(
                steam64,
                includeAppInfo = true,
                includePlayedFreeGames = false
            )
        }

        return flow {
            val reader = JsonReader(response.charStream())
            var noGames = true

            try {
                while (reader.hasNext()) {
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        reader.beginObject()
                        if (reader.nextName() == "response") {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                if (reader.nextName() == "games") {
                                    noGames = false
                                    reader.beginArray()
                                    while (reader.hasNext()) {
                                        val game = gson.fromJson<OwnedGameEntity>(
                                            reader,
                                            OwnedGameEntity::class.java
                                        )
                                        emit(game)
                                    }
                                    reader.endArray()
                                } else {
                                    reader.skipValue()
                                }
                            }
                        } else {
                            reader.skipValue()
                        }
                    }
                }
            } finally {
                reader.close()
                response.close()
            }

            if (noGames) throw GetOwnedGamesPrivacyException()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getGameStoreInfo(appId: Int): GameStoreInfo {
        val response = wrapCommonNetworkExceptions {
            steamStoreApiService.getGamesStoreInfo(listOf(appId))
        }

        val reader = JsonReader(response.charStream())
        val results = arrayMapOf<String, GameStoreInfoResult>()
        try {
            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                val obj = gson.fromJson<GameStoreInfoResult>(
                    reader,
                    GameStoreInfoResult::class.java
                )
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