package ru.luckycactus.steamroulette.data.user

import ru.luckycactus.steamroulette.domain.CachePolicy
import java.util.concurrent.TimeUnit

class UserDataStoreFactoryImpl(
    private val userCache: UserCache,
    private val remoteUserDataStore: UserDataStore,
    private val localUserDataStore: UserDataStore
) : UserDataStoreFactory {

    companion object {
        const val CACHE_WINDOW = 4L
    }

    override fun create(steam64: Long, cachePolicy: CachePolicy): UserDataStore =
        if (cachePolicy == CachePolicy.REMOTE || userCache.isExpired(steam64, CACHE_WINDOW, TimeUnit.HOURS))
            remoteUserDataStore
        else
            localUserDataStore
}

interface UserDataStoreFactory {
    fun create(steam64: Long, cachePolicy: CachePolicy): UserDataStore
}