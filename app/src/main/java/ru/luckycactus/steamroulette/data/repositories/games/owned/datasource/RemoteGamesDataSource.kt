package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.luckycactus.steamroulette.data.core.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.data.net.api.SteamApiService
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPrivacyException
import javax.inject.Inject

class RemoteGamesDataSource @Inject constructor(
    private val steamApiService: SteamApiService,
    moshi: Moshi
) : GamesDataSource.Remote {

    private val ownedGameAdapter =
        moshi.adapter(OwnedGameEntity::class.java)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getAll(steamId: SteamId): Flow<OwnedGameEntity> {
        val response = wrapCommonNetworkExceptions {
            steamApiService.getOwnedGames(
                steamId.as64(),
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
}