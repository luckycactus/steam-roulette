package ru.luckycactus.steamroulette.data.core

import android.util.LruCache
import androidx.room.withTransaction
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.presentation.common.App
import kotlin.time.Duration

private val memoryCache = LruCache<String, Any>(50)

abstract class NetworkBoundResource<RequestType, ResultType> {
    abstract suspend fun shouldFetch(cachePolicy: CachePolicy): Boolean
    abstract suspend fun fetch(): RequestType
    protected abstract suspend fun saveResult(data: RequestType)
    protected abstract suspend fun loadResult(): ResultType? //todo nbr

    suspend fun fetchIfNeed(cachePolicy: CachePolicy) {
        if (shouldFetch(cachePolicy)) {
            val data = wrapCommonNetworkExceptions { fetch() }
            saveResult(data)
        }
    }

    /**
     * throws exceptions if they were thrown during fetching or saving result
     * returns null if fetching weren't needed or finished without exceptions and there is still no cached data
     * (for example it is normal if cachePolicy == CachePolicy.Cache and there is no data)
     */
    suspend fun get(cachePolicy: CachePolicy): ResultType? {
        fetchIfNeed(cachePolicy)
        return loadResult()
    }

    suspend fun getOrThrow(cachePolicy: CachePolicy): ResultType {
        return get(cachePolicy)!!
    }

    abstract suspend fun invalidateCache()

    abstract class FullCache<RequestType, ResultType>(
        private val key: String,
        private val memoryKey: String?,
        private val window: Duration
    ) : NetworkBoundResource<RequestType, ResultType>() {

        protected abstract suspend fun saveToStorage(data: RequestType)
        protected abstract suspend fun getFromStorage(): ResultType?

        final override suspend fun saveResult(data: RequestType) {
            db.withTransaction {
                saveToStorage(data)
                cacheHelper.setCachedNow(key)
            }
            memoryCache.remove(memoryKey)
        }

        @Suppress("UNCHECKED_CAST")
        final override suspend fun loadResult(): ResultType? {
            var data: ResultType? = null

            if (memoryKey != null) {
                data = memoryCache[memoryKey] as ResultType?
            }
            if (data == null) {
                data = getFromStorage()?.also {
                    if (memoryKey != null) {
                        memoryCache.put(memoryKey, it)
                    }
                }
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
            @InstallIn(ApplicationComponent::class)
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

    abstract class MemoryCache<RequestType, ResultType>(
        private val memoryKey: String,
    ) : NetworkBoundResource<RequestType, ResultType>() {

        override suspend fun shouldFetch(cachePolicy: CachePolicy): Boolean =
            cachePolicy == CachePolicy.Remote || (cachePolicy == CachePolicy.CacheOrRemote && memoryCache[memoryKey] == null)

        override suspend fun saveResult(data: RequestType) {
            memoryCache.put(memoryKey, data)
        }

        @Suppress("UNCHECKED_CAST")
        override suspend fun loadResult(): ResultType? {
            return memoryCache[memoryKey] as ResultType?
        }

        override suspend fun invalidateCache() {
            memoryCache.remove(memoryKey)
        }
    }
}