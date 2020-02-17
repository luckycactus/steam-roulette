package ru.luckycactus.steamroulette.data.repositories.games

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.core.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.games.datastore.GamesDataStore
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.days

@Singleton
class GamesRepositoryImpl @Inject constructor(
    private val localGamesDataStore: GamesDataStore.Local,
    private val remoteGamesDataStore: GamesDataStore.Remote
) : GamesRepository {

    override suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy) {
        createOwnedGamesResource(steamId).updateIfNeed(cachePolicy)
    }

    override fun observeGamesCount(steamId: SteamId): LiveData<Int> =
        localGamesDataStore.observeOwnedGamesCount(steamId)

    override fun observeHiddenGamesCount(steamId: SteamId): LiveData<Int> =
        localGamesDataStore.observeHiddenOwnedGamesCount(steamId)

    override suspend fun resetHiddenGames(steamId: SteamId) {
        localGamesDataStore.resetHiddenOwnedGames(steamId)
    }

    override fun observeGamesUpdates(steamId: SteamId): LiveData<Long> =
        createOwnedGamesResource(steamId).observeCacheUpdates()

    override suspend fun clearUser(steamId: SteamId) {
        localGamesDataStore.clearOwnedGames(steamId)
        createOwnedGamesResource(steamId).invalidateCache()
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        localGamesDataStore.isUserHasGames(steamId)

    override suspend fun getLocalOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter
    ): List<Int> =
        localGamesDataStore.getOwnedGamesIds(steamId, filter)

    override suspend fun getLocalOwnedGame(steamId: SteamId, gameId: Int): OwnedGame =
        localGamesDataStore.getOwnedGame(steamId, gameId)

    override suspend fun getLocalOwnedGames(steamId: SteamId, gameIds: List<Int>): List<OwnedGame> =
        localGamesDataStore.getOwnedGames(steamId, gameIds)

    override suspend fun hideLocalOwnedGame(steamId: SteamId, gameId: Int) {
        localGamesDataStore.hideOwnedGame(steamId, gameId)
    }

    private fun createOwnedGamesResource(
        steamId: SteamId
    ): NetworkBoundResource<Flow<OwnedGameEntity>, List<OwnedGame>> {
        val cacheKey = "owned_games_${steamId.asSteam64()}"
        return object : NetworkBoundResource<Flow<OwnedGameEntity>, List<OwnedGame>>(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW
        ) {
            override suspend fun getFromNetwork(): Flow<OwnedGameEntity> =
                remoteGamesDataStore.getOwnedGames(steamId)

            override suspend fun saveToCache(data: Flow<OwnedGameEntity>) =
                localGamesDataStore.saveOwnedGames(steamId, data)

            override suspend fun getFromCache(): List<OwnedGame> {
                throw UnsupportedOperationException()
            }
        }
    }

    override suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo? {
        return NetworkBoundResource.withMemoryCache("game_store_info_$gameId", cachePolicy) {
            remoteGamesDataStore.getGameStoreInfo(gameId)
        }
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = 7.days
    }
}