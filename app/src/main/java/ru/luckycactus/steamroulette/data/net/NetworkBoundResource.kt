package ru.luckycactus.steamroulette.data.net

import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import java.lang.Exception
import java.util.concurrent.TimeUnit

abstract class NetworkBoundResource<RequestType, ResultType>(
    private val key: String,
    private val memoryKey: String?,
    private val windowMillis: Long
) {

    private val cacheHelper: CacheHelper = AppModule.cacheHelper
    private val memoryCache: LruCache<String, Any> = AppModule.requestLruCache

    abstract suspend fun getFromNetwork(): RequestType
    abstract suspend fun saveToCache(data: RequestType)
    abstract suspend fun getFromCache(): ResultType

    suspend fun updateIfNeed(cachePolicy: CachePolicy): Boolean {
        return if (shouldFetch(cachePolicy)) {
            val data = wrapCommonNetworkExceptions { getFromNetwork() }
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

    suspend fun getCacheThenRemoteIfExpired(coroutineScope: CoroutineScope): ReceiveChannel<ResultType> =
        coroutineScope.produce {
            try {
                getCachedData()?.let { send(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            updateIfNeed(CachePolicy.CACHE_IF_VALID).let { updated ->
                if (updated)
                    send(getCachedData()!!)
            }
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
        return cacheHelper.shouldUpdate(cachePolicy, key, windowMillis, TimeUnit.MILLISECONDS)
    }
}