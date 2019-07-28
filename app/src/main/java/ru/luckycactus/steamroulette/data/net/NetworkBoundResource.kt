package ru.luckycactus.steamroulette.data.net

import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.di.AppModule.cacheHelper
import ru.luckycactus.steamroulette.domain.CachePolicy

class NetworkBoundResource<RequestType, ResultType> private constructor(
    private val cacheHelper: CacheHelper,
    private val memoryCache: LruCache<String, Any>,
    private val key: String,
    private val memoryKey: String?,
    private val window: Long,
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
        return cacheHelper.shouldUpdate(cachePolicy, key, window)
    }

    class Factory constructor(
        private val cacheHelper: CacheHelper,
        private val memoryCache: LruCache<String, Any>
    ) {

        fun <RequestType, ResultType> create(
            key: String,
            memoryKey: String,
            window: Long,
            getFromNetwork: suspend () -> RequestType,
            saveToCache: suspend (data: RequestType) -> Unit,
            getFromCache: suspend () -> ResultType
        ): NetworkBoundResource<RequestType, ResultType> {
            return NetworkBoundResource(
                cacheHelper,
                memoryCache,
                key,
                memoryKey,
                window,
                getFromNetwork,
                saveToCache,
                getFromCache
            )
        }
    }
}