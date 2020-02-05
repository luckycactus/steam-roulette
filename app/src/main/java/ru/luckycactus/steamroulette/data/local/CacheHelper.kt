package ru.luckycactus.steamroulette.data.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.utils.apply
import ru.luckycactus.steamroulette.data.utils.longLiveData
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.domain.common.CachePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheHelper @Inject constructor(
    @Identified(R.id.cacheHelperPrefs) private val prefs: SharedPreferences
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
                (cachePolicy == CachePolicy.CacheOrRemote && !isExpired(key, window, timeUnit))


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

    fun observeCacheUpdates(key: String): LiveData<Long> = prefs.longLiveData(key, 0L)
}