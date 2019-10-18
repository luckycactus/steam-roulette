package ru.luckycactus.steamroulette.data.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.luckycactus.steamroulette.data.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import java.util.*
import java.util.concurrent.TimeUnit

class GamesRepositoryImpl(
    private val localGamesDataStore: LocalGamesDataStore,
    private val remoteGamesDataStore: RemoteGamesDataStore
) : GamesRepository {

    override suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy) {
        createOwnedGamesResource(steamId.asSteam64())
            .updateIfNeed(cachePolicy)
    }

    override fun observeGamesCount(steamId: SteamId): LiveData<Int> {
        return localGamesDataStore.observeGameCount(steamId.asSteam64())
    }

    override fun observeHiddenGamesCount(steamId: SteamId): LiveData<Int> {
        return localGamesDataStore.observeHiddenGameCount(steamId.asSteam64())
    }

    override suspend fun clearHiddenGames(steamId: SteamId) {
        localGamesDataStore.clearHiddenGames(steamId.asSteam64())
    }

    override fun observeGamesUpdates(steamId: SteamId): LiveData<Date> =
        createOwnedGamesResource(steamId.asSteam64()).observeCacheUpdates().map { Date(it) }

    override suspend fun isUserHasLocalOwnedGames(steamId: SteamId): Boolean {
        return localGamesDataStore.isUserHasOwnedGames(steamId.asSteam64())
    }

    override suspend fun getFilteredLocalOwnedGamesIds(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ): List<Int> =
        localGamesDataStore.getFilteredOwnedGamesIds(steamId.asSteam64(), filter)

    override suspend fun getLocalOwnedGame(steamId: SteamId, appId: Int): OwnedGame {
        return localGamesDataStore.getOwnedGame(steamId.asSteam64(), appId)
    }

    override suspend fun getLocalOwnedGames(steamId: SteamId, appIds: List<Int>): List<OwnedGame> {
        return localGamesDataStore.getOwnedGames(steamId.asSteam64(), appIds)
    }

    override suspend fun markLocalGameAsHidden(steamId: SteamId, appId: Int) {
        localGamesDataStore.markGameAsHidden(steamId.asSteam64(), appId)
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
        val OWNED_GAMES_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)
    }
}