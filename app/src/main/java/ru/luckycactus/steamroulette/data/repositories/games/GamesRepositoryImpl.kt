package ru.luckycactus.steamroulette.data.repositories.games

import androidx.paging.PagingSource
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.core.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.games.datasource.GamesDataSource
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
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

    override suspend fun fetchOwnedGames(cachePolicy: CachePolicy) {
        createOwnedGamesResource(currentUser).updateIfNeed(cachePolicy)
    }

    override fun observeGamesCount(): Flow<Int> =
        localGamesDataSource.observeOwnedGamesCount(currentUser)

    override fun observeHiddenGamesCount(): Flow<Int> =
        localGamesDataSource.observeHiddenOwnedGamesCount(currentUser)

    override suspend fun resetHiddenGames() {
        localGamesDataSource.resetHiddenOwnedGames(currentUser)
    }

    override fun observeGamesUpdates(): Flow<Long> =
        createOwnedGamesResource(currentUser).observeCacheUpdates()

    override suspend fun clearUser(steamId: SteamId) {
        localGamesDataSource.clearOwnedGames(steamId)
        createOwnedGamesResource(steamId).invalidateCache()
    }

    override suspend fun isUserHasGames(): Boolean =
        localGamesDataSource.isUserHasGames(currentUser)

    override suspend fun getOwnedGamesIds(
        shown: Boolean?,
        hidden: Boolean?,
        playtimeFilter: PlaytimeFilter?
    ) =
        localGamesDataSource.getOwnedGamesIds(currentUser, shown, hidden, playtimeFilter)

    override suspend fun getLocalOwnedGameHeaders(
        gameIds: List<Int>
    ): List<GameHeader> =
        localGamesDataSource.getOwnedGameHeaders(currentUser, gameIds)

    override suspend fun setLocalOwnedGamesHidden(
        gameIds: List<Int>,
        hide: Boolean
    ) {
        localGamesDataSource.setOwnedGamesHidden(currentUser, gameIds, hide)
    }

    override suspend fun setAllLocalOwnedGamesHidden(hide: Boolean) {
        localGamesDataSource.setAllOwnedGamesHidden(currentUser, hide)
    }

    override suspend fun setLocalOwnedGamesShown(
        gameIds: List<Int>,
        shown: Boolean
    ) {
        localGamesDataSource.setOwnedGamesShown(currentUser, gameIds, shown)
    }

    override suspend fun setAllLocalOwnedGamesShown(shown: Boolean) {
        localGamesDataSource.setAllOwnedGamesShown(currentUser, shown)
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
                remoteGamesDataSource.getOwnedGames(steamId)

            override suspend fun saveToCache(data: Flow<OwnedGameEntity>) =
                localGamesDataSource.updateOwnedGames(steamId, data)

            override suspend fun getFromCache() {
                throw UnsupportedOperationException()
            }
        }
    }

    override fun getOwnedGamesPagingSource(
        shown: Boolean?,
        hidden: Boolean?,
        playtimeFilter: PlaytimeFilter?
    ): PagingSource<Int, GameHeader> =
        localGamesDataSource.getOwnedGamesPagingSource(currentUser, shown, hidden, playtimeFilter)

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = 7.days
    }
}