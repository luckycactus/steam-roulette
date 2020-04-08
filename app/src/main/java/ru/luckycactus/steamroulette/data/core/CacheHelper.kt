package ru.luckycactus.steamroulette.data.core

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.milliseconds

@Singleton
class CacheHelper @Inject constructor(
    @Identified(R.id.cacheHelperPrefs) private val prefs: SharedPreferences
) {
    private val prefsEditor = prefs.edit()

    fun isCached(key: String) = prefs.getLong(key, 0) > 0L

    fun isExpired(
        key: String,
        window: Duration
    ): Boolean {
        val savedTime = prefs.getLong(key, 0L)
        if (savedTime == 0L)
            return true

        val passedTime = (System.currentTimeMillis() - savedTime).milliseconds
        return passedTime >= window
    }

    fun shouldUseCache(
        cachePolicy: CachePolicy,
        key: String,
        window: Duration
    ): Boolean = cachePolicy == CachePolicy.Cache ||
            (cachePolicy == CachePolicy.CacheOrRemote && !isExpired(key, window))

    fun shouldUpdate(
        cachePolicy: CachePolicy,
        key: String,
        window: Duration
    ): Boolean = !shouldUseCache(cachePolicy, key, window)

    fun setCachedNow(key: String) {
        prefsEditor.apply { putLong(key, System.currentTimeMillis()) }
    }

    fun invalidateCache(key: String) {
        prefsEditor.apply { remove(key) }
    }

    fun observeCacheUpdates(key: String): LiveData<Long> = prefs.longLiveData(key, 0L)
}