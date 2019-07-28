package ru.luckycactus.steamroulette.data.net

import android.util.LruCache
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.domain.CachePolicy

class NetworkBoundResource<RequestType, ResultType> private constructor(
    private val cacheHelper: CacheHelper,
    private val memoryCache: LruCache<String, Any>,
    private val key: String,
    private val memoryKey: String?,
    private val windowMillis: Long,
    private val getFromNetwork: suspend () -> RequestType,
    private val saveToCache: suspend (data: RequestType) -> Unit,
    private val getFromCache: suspend () -> ResultType
) {

    suspend fun updateIfNeed(cachePolicy: CachePolicy): Boolean {
        return if (shouldFetch(cachePolicy)) {
            val data = getFromNetwork()
            saveToCache(data)
            cacheHelper.setCachedNow(key)
            memoryCache.remove(memoryKey)
            true
        } else {
            false
        }
    }

    suspend fun get(cachePolicy: CachePolicy): ResultType {
        updateIfNeed(cachePolicy)
        return getCachedData()!!
    }

    private suspend fun getCachedData(): ResultType? {
        var data: ResultType? = null

        memoryKey?.let {
            data = memoryCache[it] as ResultType?
        }
        if (data == null) {
            data = getFromCache()
            data?.let {
                memoryKey?.let {
                    memoryCache.put(memoryKey, data)
                }
            }
        }
        return data
    }

    private fun shouldFetch(cachePolicy: CachePolicy): Boolean {
        return cacheHelper.shouldUpdate(cachePolicy, key, windowMillis)
    }

    class Factory constructor(
        private val cacheHelper: CacheHelper,
        private val memoryCache: LruCache<String, Any>
    ) {

        fun <RequestType, ResultType> create(
            key: String,
            memoryKey: String,
            windowMillis: Long,
            getFromNetwork: suspend () -> RequestType,
            saveToCache: suspend (data: RequestType) -> Unit,
            getFromCache: suspend () -> ResultType
        ): NetworkBoundResource<RequestType, ResultType> {
            return NetworkBoundResource(
                cacheHelper,
                memoryCache,
                key,
                memoryKey,
                windowMillis,
                getFromNetwork,
                saveToCache,
                getFromCache
            )
        }

        fun <RequestType, ResultType> create(
            key: String,
            windowMillis: Long,
            getFromNetwork: suspend () -> RequestType,
            saveToCache: suspend (data: RequestType) -> Unit,
            getFromCache: suspend () -> ResultType
        ): NetworkBoundResource<RequestType, ResultType> {
            return create(
                key,
                key,
                windowMillis,
                getFromNetwork,
                saveToCache,
                getFromCache
            )
        }
    }
}