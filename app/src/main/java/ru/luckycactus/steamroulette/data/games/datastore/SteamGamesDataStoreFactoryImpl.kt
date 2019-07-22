package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.games.cache.SteamGamesCache
import ru.luckycactus.steamroulette.domain.CachePolicy

class SteamGamesDataStoreFactoryImpl(
    private val localSteamGamesDataStore: LocalSteamGamesDataStore,
    private val remoteSteamGamesDataStore: RemoteSteamGamesDataStore,
    private val steamGamesCache: SteamGamesCache
) : SteamGamesDataStoreFactory {

    override fun create(cachePolicy: CachePolicy): SteamGamesDataStore {
        return if (cachePolicy == CachePolicy.REMOTE || steamGamesCache.isExpired())
            remoteSteamGamesDataStore
        else
            localSteamGamesDataStore
    }
}

interface SteamGamesDataStoreFactory {

    fun create(cachePolicy: CachePolicy): SteamGamesDataStore
}