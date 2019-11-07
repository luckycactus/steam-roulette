package ru.luckycactus.steamroulette.data.games.datastore

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.utils.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.domain.exception.GetOwnedGamesPrivacyException
import javax.inject.Inject

@Reusable
class RemoteGamesDataStore @Inject constructor(
    private val steamApiService: SteamApiService,
    private val gson: Gson
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
}