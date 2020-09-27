package ru.luckycactus.steamroulette.data.repositories.games

import androidx.paging.PagingSource
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
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import kotlin.time.days

@Reusable
class GamesRepositoryImpl @Inject constructor(
    private val localGamesDataStore: GamesDataStore.Local,
    private val remoteGamesDataStore: GamesDataStore.Remote,
    private val gameStoreInfoEntityMapper: GameStoreInfoEntityMapper,
    private val userSession: UserSession
) : GamesRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    override suspend fun fetchOwnedGames(cachePolicy: CachePolicy) {
        createOwnedGamesResource(currentUser).updateIfNeed(cachePolicy)
    }

    override fun observeGamesCount(): Flow<Int> =
        localGamesDataStore.observeOwnedGamesCount(currentUser)

    override fun observeHiddenGamesCount(): Flow<Int> =
        localGamesDataStore.observeHiddenOwnedGamesCount(currentUser)

    override fun getHiddenGamesPagingSource(): PagingSource<Int, GameHeader> =
        localGamesDataStore.getHiddenGamesPagingSource(currentUser)

    override suspend fun resetHiddenGames() {
        localGamesDataStore.resetHiddenOwnedGames(currentUser)
    }

    override fun observeGamesUpdates(): Flow<Long> =
        createOwnedGamesResource(currentUser).observeCacheUpdates()

    override suspend fun clearUser(steamId: SteamId) {
        localGamesDataStore.clearOwnedGames(steamId)
        createOwnedGamesResource(steamId).invalidateCache()
    }

    override suspend fun isUserHasGames(): Boolean =
        localGamesDataStore.isUserHasGames(currentUser)

    override suspend fun getOwnedGamesIds(
        shown: Boolean?,
        hidden: Boolean?,
        playtimeFilter: PlaytimeFilter?
    ) =
        localGamesDataStore.getOwnedGamesIds(currentUser, shown, hidden, playtimeFilter)

    override suspend fun getLocalOwnedGameHeaders(
        gameIds: List<Int>
    ): List<GameHeader> =
        localGamesDataStore.getOwnedGameHeaders(currentUser, gameIds)

    override suspend fun setLocalOwnedGamesHidden(
        gameIds: List<Int>,
        hide: Boolean
    ) {
        localGamesDataStore.setOwnedGamesHidden(currentUser, gameIds, hide)
    }

    override suspend fun setAllLocalOwnedGamesHidden(hide: Boolean) {
        localGamesDataStore.setAllOwnedGamesHidden(currentUser, hide)
    }

    override suspend fun setLocalOwnedGamesShown(
        gameIds: List<Int>,
        shown: Boolean
    ) {
        localGamesDataStore.setOwnedGamesShown(currentUser, gameIds, shown)
    }

    override suspend fun setAllLocalOwnedGamesShown(shown: Boolean) {
        localGamesDataStore.setAllOwnedGamesShown(currentUser, shown)
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