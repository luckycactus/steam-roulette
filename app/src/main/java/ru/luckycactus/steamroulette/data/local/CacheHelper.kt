package ru.luckycactus.steamroulette.data.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.apply
import ru.luckycactus.steamroulette.data.longLiveData
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import java.util.concurrent.TimeUnit

class CacheHelper(
    private val prefs: SharedPreferences
) {
    private val prefsEditor = prefs.edit()

    fun isCached(key: String): Boolean {
        return prefs.getLong(key, 0) > 0L
    }

    fun isExpired(
        key: String,
        window: Long,
        timeUnit: TimeUnit = TimeUnit.HOURS
    ): Boolean {
        val savedTime = prefs.getLong(key, 0)
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
        prefsEditor.apply { putLong(key, System.currentTimeMillis()) }
    }

    fun invalidateCache(key: String) {
        prefsEditor.apply { remove(key) }
    }

    fun observeCacheUpdates(key: String): LiveData<Long> = prefs.longLiveData(key, 0)
}