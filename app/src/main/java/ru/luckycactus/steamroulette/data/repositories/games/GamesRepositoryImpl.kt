package ru.luckycactus.steamroulette.data.repositories.games

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.core.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.games.datastore.GamesDataStore
import ru.luckycactus.steamroulette.data.repositories.games.mapper.GameStoreInfoEntityMapper
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject
import kotlin.time.days

@Reusable
class GamesRepositoryImpl @Inject constructor(
    private val localGamesDataStore: GamesDataStore.Local,
    private val remoteGamesDataStore: GamesDataStore.Remote,
    private val gameStoreInfoEntityMapper: GameStoreInfoEntityMapper
) : GamesRepository {

    override suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy) {
        createOwnedGamesResource(steamId).updateIfNeed(cachePolicy)
    }

    override fun observeGamesCount(steamId: SteamId): Flow<Int> =
        localGamesDataStore.observeOwnedGamesCount(steamId)

    override fun observeHiddenGamesCount(steamId: SteamId): Flow<Int> =
        localGamesDataStore.observeHiddenOwnedGamesCount(steamId)

    override fun getHiddenGamesPagedListLiveData(steamId: SteamId): LiveData<PagedList<GameHeader>> =
        localGamesDataStore.getHiddenGamesDataSourceFactory(steamId).toLiveData(pageSize = 50)

    override suspend fun resetHiddenGames(steamId: SteamId) {
        localGamesDataStore.resetHiddenOwnedGames(steamId)
    }

    override fun observeGamesUpdates(steamId: SteamId): Flow<Long> =
        createOwnedGamesResource(steamId).observeCacheUpdates()

    override suspend fun clearUser(steamId: SteamId) {
        localGamesDataStore.clearOwnedGames(steamId)
        createOwnedGamesResource(steamId).invalidateCache()
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        localGamesDataStore.isUserHasGames(steamId)

    override suspend fun getVisibleLocalOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter,
        shown: Boolean
    ): List<Int> =
        localGamesDataStore.getVisibleOwnedGamesIds(steamId, filter, shown)

    override suspend fun getLocalOwnedGameHeaders(
        steamId: SteamId,
        gameIds: List<Int>
    ): List<GameHeader> =
        localGamesDataStore.getOwnedGameHeaders(steamId, gameIds)

    override suspend fun setLocalOwnedGamesHidden(
        steamId: SteamId,
        gameIds: List<Int>,
        hide: Boolean
    ) {
        localGamesDataStore.setOwnedGamesHidden(steamId, gameIds, hide)
    }

    override suspend fun setAllLocalOwnedGamesHidden(steamId: SteamId, hide: Boolean) {
        localGamesDataStore.setAllOwnedGamesHidden(steamId, hide)
    }

    override suspend fun setLocalOwnedGamesShown(
        steamId: SteamId,
        gameIds: List<Int>,
        shown: Boolean
    ) {
        localGamesDataStore.setOwnedGamesShown(steamId, gameIds, shown)
    }

    override suspend fun setAllLocalOwnedGamesShown(steamId: SteamId, shown: Boolean) {
        localGamesDataStore.setAllOwnedGamesShown(steamId, shown)
    }

    private fun createOwnedGamesResource(
        steamId: SteamId
    ): NetworkBoundResource<Flow<OwnedGameEntity>, Unit> {
        val cacheKey = "owned_games_${steamId.as64()}"
        return object : NetworkBoundResource<Flow<OwnedGameEntity>, Unit>(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW
        ) {
            override suspend fun getFromNetwork(): Flow<OwnedGameEntity> =
                remoteGamesDataStore.getOwnedGames(steamId)

            override suspend fun saveToCache(data: Flow<OwnedGameEntity>) =
                localGamesDataStore.updateOwnedGames(steamId, data)

            override suspend fun getFromCache() {
                throw UnsupportedOperationException()
            }
        }
    }

    override suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo? {
        return NetworkBoundResource.withMemoryCache("game_store_info_$gameId", cachePolicy) {
            gameStoreInfoEntityMapper.mapFrom(remoteGamesDataStore.getGameStoreInfo(gameId))
        }
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = 7.days
    }
}