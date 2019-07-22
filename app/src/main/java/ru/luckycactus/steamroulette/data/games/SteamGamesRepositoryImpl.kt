package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.data.games.datastore.SteamGamesDataStoreFactory
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.domain.CachePolicy
import ru.luckycactus.steamroulette.domain.OwnedGame

class SteamGamesRepositoryImpl(
    private val steamGamesDataStoreFactory: SteamGamesDataStoreFactory,
    private val ownedGameMapper: OwnedGameMapper
) : SteamGamesRepository {

    override suspend fun getOwnedGames(userId: Long, reload: Boolean): List<OwnedGame> {
        val cachePolicy = if (reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        val dataStore = steamGamesDataStoreFactory.create(cachePolicy)
        val entityList =  dataStore.getOwnedGames(userId, includeAppInfo = true, includePlayedFreeGames = false) //todo
        return ownedGameMapper.mapFrom(entityList)
    }
}