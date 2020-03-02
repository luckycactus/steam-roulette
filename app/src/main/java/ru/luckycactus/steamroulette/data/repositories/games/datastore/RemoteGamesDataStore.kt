package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.collection.arrayMapOf
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.luckycactus.steamroulette.data.core.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.data.net.services.SteamApiService
import ru.luckycactus.steamroulette.data.net.services.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.data.repositories.games.models.GameStoreInfoResult
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.GetGameStoreInfoException
import ru.luckycactus.steamroulette.domain.common.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RemoteGamesDataStore @Inject constructor(
    private val steamApiService: SteamApiService,
    private val steamStoreApiService: SteamStoreApiService,
    @Named("api") private val moshi: Moshi,
    private val languageProvider: LanguageProvider
) : GamesDataStore.Remote {

    private val ownedGameAdapter =
        moshi.adapter<OwnedGameEntity>(OwnedGameEntity::class.java)
    private val gameStoreInfoResultAdapter =
        moshi.adapter<GameStoreInfoResult>(GameStoreInfoResult::class.java)

    override suspend fun getOwnedGames(steamId: SteamId): Flow<OwnedGameEntity> {
        val response = wrapCommonNetworkExceptions {
            steamApiService.getOwnedGames(
                steamId.asSteam64(),
                includeAppInfo = true,
                includePlayedFreeGames = false
            )
        }

        return flow {
            val reader = JsonReader.of(response.source())
            var noGames = true

            try {
                while (reader.hasNext()) {
                    if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                        reader.beginObject()
                        if (reader.nextName() == "response") {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                if (reader.nextName() == "games") {
                                    noGames = false
                                    reader.beginArray()
                                    while (reader.hasNext()) {
                                        val game = ownedGameAdapter.fromJson(reader)!!
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

    //todo document
    override suspend fun getGameStoreInfo(appId: Int): GameStoreInfoEntity {
        val response = wrapCommonNetworkExceptions {
            steamStoreApiService.getGamesStoreInfo(
                listOf(appId),
                languageProvider.getLanguageForStoreApi()
            )
        }

        val reader = JsonReader.of(response.source()) //todo moshi
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