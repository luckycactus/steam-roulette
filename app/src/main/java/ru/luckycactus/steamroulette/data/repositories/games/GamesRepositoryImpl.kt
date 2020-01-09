package ru.luckycactus.steamroulette.data.repositories.games

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.datastore.GamesDataStore
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesRepositoryImpl @Inject constructor(
    private val localGamesDataStore: GamesDataStore.Local,
    private val remoteGamesDataStore: GamesDataStore.Remote
) : GamesRepository {

    override suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy) {
        createOwnedGamesResource(steamId.asSteam64())
            .updateIfNeed(cachePolicy)
    }

    override fun observeGamesCount(steamId: SteamId): LiveData<Int> =
        localGamesDataStore.observeOwnedGamesCount(steamId.asSteam64())

    override fun observeHiddenGamesCount(steamId: SteamId): LiveData<Int> =
        localGamesDataStore.observeHiddenOwnedGamesCount(steamId.asSteam64())

    override suspend fun clearHiddenGames(steamId: SteamId) {
        localGamesDataStore.clearHiddenOwnedGames(steamId.asSteam64())
    }

    override fun observeGamesUpdates(steamId: SteamId): LiveData<Long> =
        createOwnedGamesResource(steamId.asSteam64()).observeCacheUpdates()

    override suspend fun clearUser(steamId: SteamId) {
        localGamesDataStore.clearOwnedGames(steamId.asSteam64())
        createOwnedGamesResource(steamId.asSteam64())
            .invalidateCache()
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        localGamesDataStore.isUserHasGames(steamId.asSteam64())

    override suspend fun getFilteredLocalOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter
    ): List<Int> =
        localGamesDataStore.getFilteredOwnedGamesIds(steamId.asSteam64(), filter)

    override suspend fun getLocalOwnedGame(steamId: SteamId, gameId: Int): OwnedGame {
        return localGamesDataStore.getOwnedGame(steamId.asSteam64(), gameId)
    }

    override suspend fun getLocalOwnedGames(steamId: SteamId, gameIds: List<Int>): List<OwnedGame> {
        return localGamesDataStore.getOwnedGames(steamId.asSteam64(), gameIds)
    }

    override suspend fun markLocalGameAsHidden(steamId: SteamId, gameId: Int) {
        localGamesDataStore.hideOwnedGame(steamId.asSteam64(), gameId)
    }

    private fun createOwnedGamesResource(
        steam64: Long
    ): NetworkBoundResource<Flow<OwnedGameEntity>, List<OwnedGame>> {
        val cacheKey = "owned_games_$steam64"
        return object : NetworkBoundResource<Flow<OwnedGameEntity>, List<OwnedGame>>(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW
        ) {
            override suspend fun getFromNetwork(): Flow<OwnedGameEntity> =
                remoteGamesDataStore.getOwnedGames(steam64)

            override suspend fun saveToCache(data: Flow<OwnedGameEntity>) =
                localGamesDataStore.saveOwnedGames(steam64, data)

            override suspend fun getFromCache(): List<OwnedGame> {
                throw UnsupportedOperationException()
            }
        }
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)
    }
}