package ru.luckycactus.steamroulette.data.core

import android.util.LruCache
import androidx.room.withTransaction
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.presentation.common.App
import kotlin.time.Duration

private val memoryCache = MemoryCache()

abstract class NetworkBoundResource<RequestType : Any?, ResultType : Any?> {
    abstract suspend fun shouldFetch(cachePolicy: CachePolicy): Boolean
    abstract suspend fun fetch(): RequestType
    protected abstract suspend fun saveResult(data: RequestType)
    protected abstract suspend fun loadResult(): ResultType //todo nbr

    suspend fun fetchIfNeed(cachePolicy: CachePolicy) {
        if (shouldFetch(cachePolicy)) {
            val data = fetch()
            saveResult(data)
        }
    }

    suspend fun getOrThrow(cachePolicy: CachePolicy): ResultType {
        return get(cachePolicy)!!
    }

    suspend fun get(cachePolicy: CachePolicy): ResultType {
        fetchIfNeed(cachePolicy)
        return loadResult()
    }

    abstract suspend fun invalidateCache()
}

abstract class FullCacheNbr<RequestType : Any?, ResultType: Any?>(
    private val key: String,
    private val memoryKey: String?,
    private val window: Duration
) : NetworkBoundResource<RequestType, ResultType>() {

    protected abstract suspend fun saveToStorage(data: RequestType)
    protected abstract suspend fun getFromStorage(): ResultType

    final override suspend fun saveResult(data: RequestType) {
        db.withTransaction {
            saveToStorage(data)
            cacheHelper.setCachedNow(key)
        }
        memoryCache.remove(memoryKey)
    }

    final override suspend fun loadResult(): ResultType {
        if (memoryKey != null && memoryKey in memoryCache) {
            return memoryCache.getValue(memoryKey)
        }
        val data = getFromStorage()
        if (memoryKey != null) {
            memoryCache[memoryKey] = data
        }
        return data
    }

    fun observeCacheUpdates(): Flow<Long> = cacheHelper.observeCacheUpdates(key)

    final override suspend fun invalidateCache() {
        cacheHelper.remove(key)
        memoryCache.remove(memoryKey)
    }

    final override suspend fun shouldFetch(cachePolicy: CachePolicy): Boolean {
        return cacheHelper.shouldUpdate(cachePolicy, key, window)
    }

    companion object {
        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface FullCacheNBRCompanionEntryPoint {
            fun cacheHelper(): CacheHelper
            fun db(): AppDatabase
        }

        private val cacheHelper: CacheHelper
        private val db: AppDatabase

        init {
            with(
                EntryPointAccessors.fromApplication(
                    App.getInstance(),
                    FullCacheNBRCompanionEntryPoint::class.java
                )
            ) {
                cacheHelper = cacheHelper()
                db = db()
            }
        }
    }
}

abstract class MemoryCacheNbr<RequestType : Any?, ResultType: Any?>(
    private val memoryKey: String,
) : NetworkBoundResource<RequestType, ResultType>() {

    override suspend fun shouldFetch(cachePolicy: CachePolicy): Boolean =
        cachePolicy == CachePolicy.Remote ||
                (cachePolicy == CachePolicy.CacheOrRemote && memoryKey !in memoryCache)

    override suspend fun saveResult(data: RequestType) {
        memoryCache[memoryKey] = data
    }

    override suspend fun loadResult(): ResultType = memoryCache.getValue(memoryKey)

    override suspend fun invalidateCache() {
        memoryCache.remove(memoryKey)
    }
}

private class MemoryCache {
    private val cache = LruCache<String, Any?>(50)

    fun remove(key: String?) {
        cache.remove(key)
    }

    operator fun <T> get(key: String): T? {
        val data = cache[key]
        if (data === NULL) return null
        return data as T?
    }

    fun <T : Any?> getValue(key: String): T {
        val data = cache[key]
        if (data === NULL) return null as T
        return data as T
    }

    operator fun <T> set(key: String, value: T) {
        if (value == null) {
            cache.put(key, NULL)
        } else {
            cache.put(key, value)
        }
    }

    operator fun contains(key: String) = cache[key] != null

    companion object {
        private val NULL = Object()
    }
}