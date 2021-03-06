package ru.luckycactus.steamroulette.data.repositories.games.owned

import androidx.paging.PagingSource
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.core.FullCacheNbr
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesDataSource
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import kotlin.time.days

@Reusable
class GamesRepositoryImpl @Inject constructor(
    private val localGamesDataSource: GamesDataSource.Local,
    private val remoteGamesDataSource: GamesDataSource.Remote,
    private val userSession: UserSession
) : GamesRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    override suspend fun updateOwnedGames(cachePolicy: CachePolicy) {
        createOwnedGamesResource(currentUser).fetchIfNeed(cachePolicy)
    }

    override fun observeGamesCount(filter: GamesFilter): Flow<Int> =
        localGamesDataSource.observeCount(currentUser, filter)

    override suspend fun resetHiddenGames() {
        localGamesDataSource.resetAllHidden(currentUser)
    }

    override fun observeGamesUpdates(): Flow<Long> =
        createOwnedGamesResource(currentUser).observeCacheUpdates()

    override suspend fun clearUser(steamId: SteamId) {
        localGamesDataSource.clear(steamId)
        createOwnedGamesResource(steamId).invalidateCache()
    }

    override suspend fun isUserHasGames(): Boolean =
        localGamesDataSource.isUserHasGames(currentUser)

    override suspend fun getOwnedGamesIds(gamesFilter: GamesFilter, orderById: Boolean) =
        localGamesDataSource.getIds(currentUser, gamesFilter, orderById)

    override suspend fun getOwnedGamesIdsMutable(
        gamesFilter: GamesFilter,
        orderById: Boolean
    ) = localGamesDataSource.getIdsMutable(currentUser, gamesFilter, orderById)


    override suspend fun getOwnedGameHeaders(gameIds: List<Int>): List<GameHeader> =
        localGamesDataSource.getHeaders(currentUser, gameIds)

    override suspend fun setOwnedGamesHidden(
        gameIds: List<Int>,
        hide: Boolean
    ) {
        localGamesDataSource.setHidden(currentUser, gameIds, hide)
    }

    override suspend fun setAllOwnedGamesHidden(hide: Boolean) {
        localGamesDataSource.setAllHidden(currentUser, hide)
    }

    override suspend fun setOwnedGamesShown(
        gameIds: List<Int>,
        shown: Boolean
    ) {
        localGamesDataSource.setShown(currentUser, gameIds, shown)
    }

    override suspend fun setAllOwnedGamesShown(shown: Boolean) {
        localGamesDataSource.setAllShown(currentUser, shown)
    }

    private fun createOwnedGamesResource(
        steamId: SteamId
    ): FullCacheNbr<Flow<OwnedGameEntity>, Unit> {
        val cacheKey = "owned_games_${steamId.as64()}"
        return object : FullCacheNbr<Flow<OwnedGameEntity>, Unit>(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW
        ) {
            override suspend fun fetch(): Flow<OwnedGameEntity> =
                remoteGamesDataSource.getAll(steamId)

            override suspend fun saveToStorage(data: Flow<OwnedGameEntity>) =
                localGamesDataSource.update(steamId, data)

            override suspend fun getFromStorage() {
                throw UnsupportedOperationException()
            }
        }
    }

    override fun getLibraryPagingSource(
        filter: GamesFilter,
        nameSearchQuery: String?
    ): PagingSource<Int, LibraryGame> =
        localGamesDataSource.getLibraryPagingSource(currentUser, filter, nameSearchQuery)

    override suspend fun getOwnedGameHiddenState(appId: Long): Boolean {
        return localGamesDataSource.getHiddenState(currentUser, appId)
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = 7.days
    }
}