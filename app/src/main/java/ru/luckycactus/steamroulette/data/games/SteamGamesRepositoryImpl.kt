package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.data.games.datastore.LocalSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.domain.CachePolicy
import ru.luckycactus.steamroulette.domain.OwnedGame
import java.util.concurrent.TimeUnit

class SteamGamesRepositoryImpl(
    private val localSteamGamesDataStore: LocalSteamGamesDataStore,
    private val remoteSteamGamesDataStore: RemoteSteamGamesDataStore,
    private val ownedGameMapper: OwnedGameMapper,
    private val networkBoundResourceFactory: NetworkBoundResource.Factory
) : SteamGamesRepository {

    override suspend fun getOwnedGames(steam64: Long, cachePolicy: CachePolicy): List<OwnedGame> {
        val cacheKey = "owned_games_$steam64"
        return networkBoundResourceFactory.create(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW,
            getFromNetwork = { remoteSteamGamesDataStore.getOwnedGames(steam64) },
            saveToCache = { localSteamGamesDataStore.saveOwnedGamesToCache(it) },
            getFromCache = { ownedGameMapper.mapFrom(localSteamGamesDataStore.getOwnedGames(steam64)) }
        ).get(cachePolicy)
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)
    }
}