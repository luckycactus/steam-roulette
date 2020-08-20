package ru.luckycactus.steamroulette.data.core

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.Clock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.milliseconds

@Singleton
class CacheHelper @Inject constructor(
    @Named("cache-helper") private val prefs: SharedPreferences,
    private val clock: Clock
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

        val passedTime = (clock.currentTimeMillis() - savedTime).milliseconds
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
        prefsEditor.edit { putLong(key, clock.currentTimeMillis()) }
    }

    fun remove(key: String) {
        prefsEditor.edit { remove(key) }
    }

    fun clear() {
        prefsEditor.edit { clear() }
    }

    fun observeCacheUpdates(key: String): Flow<Long> = prefs.longFlow(key, 0L)
}