package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.data.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import java.util.concurrent.TimeUnit

class GamesRepositoryImpl(
    private val localGamesDataStore: LocalGamesDataStore,
    private val remoteGamesDataStore: RemoteGamesDataStore,
    private val networkBoundResourceFactory: NetworkBoundResource.Factory
) : GamesRepository {

    override suspend fun getOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): List<OwnedGame> =
        createOwnedGamesResource(steamId.asSteam64())
            .get(cachePolicy)

    override suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy) {
        createOwnedGamesResource(steamId.asSteam64())
            .updateIfNeed(cachePolicy)
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
        return networkBoundResourceFactory.create(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW,
            getFromNetwork = { remoteGamesDataStore.getOwnedGames(steam64) },
            saveToCache = { localGamesDataStore.saveOwnedGamesToCache(steam64, it) },
            getFromCache = { localGamesDataStore.getOwnedGames(steam64) }
        )
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)
    }
}