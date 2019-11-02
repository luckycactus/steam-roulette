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

    fun isCached(key: String) = prefs.getLong(key, 0) > 0L

    fun isExpired(
        key: String,
        window: Long,
        timeUnit: TimeUnit = TimeUnit.HOURS
    ): Boolean {
        val savedTime = prefs.getLong(key, 0L)
        if (savedTime == 0L)
            return true
        val expireTime = savedTime + TimeUnit.MILLISECONDS.convert(window, timeUnit)
        return System.currentTimeMillis() >= expireTime
    }

    fun shouldUseCache(
        cachePolicy: CachePolicy,
        key: String,
        window: Long,
        timeUnit: TimeUnit = TimeUnit.HOURS
    ): Boolean =
        cachePolicy == CachePolicy.OnlyCache ||
                (cachePolicy == CachePolicy.CacheIfValid && !isExpired(key, window, timeUnit))


    fun shouldUpdate(
        cachePolicy: CachePolicy,
        key: String,
        window: Long,
        timeUnit: TimeUnit = TimeUnit.HOURS
    ): Boolean =
        !shouldUseCache(cachePolicy, key, window, timeUnit)

    fun setCachedNow(key: String) {
        //todo apply or commit
        prefsEditor.apply { putLong(key, System.currentTimeMillis()) }
    }

    fun invalidateCache(key: String) {
        prefsEditor.apply { remove(key) }
    }

    fun observeCacheUpdates(key: String): LiveData<Long> = prefs.longLiveData(key, 0)
}