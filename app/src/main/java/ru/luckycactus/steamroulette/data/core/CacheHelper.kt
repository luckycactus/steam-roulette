package ru.luckycactus.steamroulette.data.core

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.local.db.CacheInfoRoomEntity
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.milliseconds

interface CacheHelper {
    suspend fun isCached(key: String): Boolean
    suspend fun isExpired(key: String, window: Duration): Boolean
    suspend fun shouldUseCache(cachePolicy: CachePolicy, key: String, window: Duration): Boolean
    suspend fun shouldUpdate(cachePolicy: CachePolicy, key: String, window: Duration): Boolean
    suspend fun setCachedNow(key: String)
    suspend fun remove(key: String)
    suspend fun clear()
    fun observeCacheUpdates(key: String): Flow<Long>
}

@Singleton
class RoomCacheHelper @Inject constructor(
    db: AppDatabase,
    private val clock: Clock
) : CacheHelper {
    private val cacheInfoDao = db.cacheInfoDao()

    override suspend fun isCached(key: String) = cacheInfoDao.getTimestamp(key) > 0L

    override suspend fun isExpired(
        key: String,
        window: Duration
    ): Boolean {
        val savedTime = cacheInfoDao.getTimestamp(key)
        if (savedTime == 0L)
            return true

        val passedTime = (clock.currentTimeMillis() - savedTime).milliseconds
        return passedTime >= window
    }

    override suspend fun shouldUseCache(
        cachePolicy: CachePolicy,
        key: String,
        window: Duration
    ): Boolean = cachePolicy == CachePolicy.Cache ||
            (cachePolicy == CachePolicy.CacheOrRemote && !isExpired(key, window))

    override suspend fun shouldUpdate(
        cachePolicy: CachePolicy,
        key: String,
        window: Duration
    ): Boolean = !shouldUseCache(cachePolicy, key, window)

    override suspend fun setCachedNow(key: String) {
        cacheInfoDao.upsert(CacheInfoRoomEntity(key, clock.currentTimeMillis()))
    }

    override suspend fun remove(key: String) {
        cacheInfoDao.delete(key)
    }

    override suspend fun clear() {
        cacheInfoDao.clear()
    }

    override fun observeCacheUpdates(key: String): Flow<Long> = cacheInfoDao.observeTimestamp(key)
}