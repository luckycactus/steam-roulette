package ru.luckycactus.steamroulette.data.local

import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import java.util.concurrent.TimeUnit

class CacheHelper(
    private val prefs: PreferencesStorage
) {

    fun isCached(key: String): Boolean {
        return prefs.getLong(key) > 0L
    }

    fun isExpired(
        key: String,
        window: Long,
        timeUnit: TimeUnit = TimeUnit.HOURS
    ): Boolean {
        val savedTime = prefs.getLong(key)
        val expireTime = savedTime + TimeUnit.MILLISECONDS.convert(window, timeUnit)
        return System.currentTimeMillis() >= expireTime
    }

    fun shouldUseCache(
        cachePolicy: CachePolicy,
        key: String,
        window: Long,
        timeUnit: TimeUnit = TimeUnit.HOURS
    ): Boolean =
        cachePolicy == CachePolicy.ONLY_CACHE ||
                cachePolicy == CachePolicy.CACHE_IF_VALID && !isExpired(key, window, timeUnit)


    fun shouldUpdate(
        cachePolicy: CachePolicy,
        key: String,
        window: Long,
        timeUnit: TimeUnit = TimeUnit.HOURS
    ): Boolean =
        !shouldUseCache(cachePolicy, key, window, timeUnit)

    fun setCachedNow(key: String) {
        prefs[key] = System.currentTimeMillis()
    }

    fun invalidateCache(key: String) {
        prefs.remove(key)
    }
}