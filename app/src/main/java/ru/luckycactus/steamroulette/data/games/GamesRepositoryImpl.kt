package ru.luckycactus.steamroulette.data.games

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import java.util.*
import java.util.concurrent.TimeUnit

class GamesRepositoryImpl(
    private val localGamesDataStore: LocalGamesDataStore,
    private val remoteGamesDataStore: RemoteGamesDataStore
) : GamesRepository {

    override suspend fun getOwnedGames(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): List<OwnedGame> =
        createOwnedGamesResource(steamId.asSteam64())
            .get(cachePolicy)

    override suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy) {
        createOwnedGamesResource(steamId.asSteam64())
            .updateIfNeed(cachePolicy)
    }

    override fun observeGamesCount(steamId: SteamId): LiveData<Int> {
        return localGamesDataStore.observeGameCount(steamId.asSteam64())
    }

    override fun observeGamesUpdates(steamId: SteamId): LiveData<Date> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun isUserHasLocalOwnedGames(steamId: SteamId): Boolean {
        return localGamesDataStore.isUserHasOwnedGames(steamId.asSteam64())
    }


    override suspend fun getFilteredLocalOwnedGamesIds(steamId: SteamId): List<Int> =
        localGamesDataStore.getFilteredOwnedGamesIds(steamId.asSteam64())

    override suspend fun getLocalOwnedGame(steamId: SteamId, appId: Int): OwnedGame {
        return localGamesDataStore.getOwnedGame(steamId.asSteam64(), appId)
    }

    override suspend fun markLocalGameAsHidden(steamId: SteamId, ownedGame: OwnedGame) {
        localGamesDataStore.markGameAsHidden(steamId.asSteam64(), ownedGame.appId)
    }

    private fun createOwnedGamesResource(steam64: Long): NetworkBoundResource<List<OwnedGameEntity>, List<OwnedGame>> {
        val cacheKey = "owned_games_$steam64"
        return object : NetworkBoundResource<List<OwnedGameEntity>, List<OwnedGame>>(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW
        ) {
            override suspend fun getFromNetwork(): List<OwnedGameEntity> =
                remoteGamesDataStore.getOwnedGames(steam64)

            override suspend fun saveToCache(data: List<OwnedGameEntity>) =
                localGamesDataStore.saveOwnedGamesToCache(steam64, data)

            override suspend fun getFromCache(): List<OwnedGame> =
                localGamesDataStore.getOwnedGames(steam64)
        }
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)
    }
}