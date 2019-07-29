package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.data.games.datastore.LocalSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import java.util.concurrent.TimeUnit

class SteamGamesRepositoryImpl(
    private val localSteamGamesDataStore: LocalSteamGamesDataStore,
    private val remoteSteamGamesDataStore: RemoteSteamGamesDataStore,
    private val ownedGameMapper: OwnedGameMapper,
    private val networkBoundResourceFactory: NetworkBoundResource.Factory
) : SteamGamesRepository {

    override suspend fun getOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): List<OwnedGame> {
        val cacheKey = "owned_games_${steamId.asSteam64()}"
        return networkBoundResourceFactory.create(
            cacheKey,
            cacheKey,
            OWNED_GAMES_CACHE_WINDOW,
            getFromNetwork = { remoteSteamGamesDataStore.getOwnedGames(steamId.asSteam64()) },
            saveToCache = { localSteamGamesDataStore.saveOwnedGamesToCache(it) },
            getFromCache = { ownedGameMapper.mapFrom(localSteamGamesDataStore.getOwnedGames(steamId.asSteam64())) }
        ).get(cachePolicy)
    }

    override suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): Int {
        val cacheKey = "owned_games_${steamId.asSteam64()}"
        val memoryKey = "owned_games_count_${steamId.asSteam64()}"
        return networkBoundResourceFactory.create(
            cacheKey,
            memoryKey,
            OWNED_GAMES_CACHE_WINDOW,
            getFromNetwork = { remoteSteamGamesDataStore.getOwnedGames(steamId.asSteam64()) },
            saveToCache = { localSteamGamesDataStore.saveOwnedGamesToCache(it) },
            getFromCache = { localSteamGamesDataStore.getOwnedGamesCount(steamId.asSteam64()) }
        ).get(cachePolicy)
    }

    companion object {
        val OWNED_GAMES_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)
    }
}