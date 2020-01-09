package ru.luckycactus.steamroulette.data.net

import android.util.LruCache
import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.utils.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.di.common.AppComponent
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.domain.common.CachePolicy
import java.util.concurrent.TimeUnit

abstract class NetworkBoundResource<RequestType, ResultType>(
    private val key: String,
    private val memoryKey: String?,
    private val windowMillis: Long
) {
    abstract suspend fun getFromNetwork(): RequestType
    abstract suspend fun saveToCache(data: RequestType)
    abstract suspend fun getFromCache(): ResultType

    suspend fun updateIfNeed(cachePolicy: CachePolicy) {
        if (shouldUpdate(cachePolicy)) {
            val data = wrapCommonNetworkExceptions { getFromNetwork() }
            saveToCache(data)
            cacheHelper.setCachedNow(key)
            memoryCache.remove(memoryKey)
        }
    }

    suspend fun get(cachePolicy: CachePolicy): ResultType {
        updateIfNeed(cachePolicy)
        return getCachedData()!!
    }

    fun observeCacheUpdates(): LiveData<Long> = cacheHelper.observeCacheUpdates(key)

    fun invalidateCache() {
        cacheHelper.invalidateCache(key)
        memoryCache.remove(memoryKey)
    }

    private suspend fun getCachedData(): ResultType? {
        var data: ResultType? = null

        if (memoryKey != null) {
            data = memoryCache[memoryKey] as ResultType?
        }
        if (data == null) {
            data = getFromCache()
            data?.let {
                if (memoryKey != null) {
                    memoryCache.put(memoryKey, data)
                }
            }
        }
        return data
    }

    private fun shouldUpdate(cachePolicy: CachePolicy): Boolean {
        return cacheHelper.shouldUpdate(cachePolicy, key, windowMillis, TimeUnit.MILLISECONDS)
    }

    companion object {
        private val memoryCache = LruCache<String, Any>(50)
        private val cacheHelper: CacheHelper =
            InjectionManager.findComponent<AppComponent>().cacheHelper
    }
}