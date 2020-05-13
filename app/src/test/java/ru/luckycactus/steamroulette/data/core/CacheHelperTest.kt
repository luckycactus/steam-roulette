package ru.luckycactus.steamroulette.data.core

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.di.common.StorageModule
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.Clock
import kotlin.time.Duration
import kotlin.time.days
import kotlin.time.milliseconds
import kotlin.time.minutes

@RunWith(AndroidJUnit4::class)
class CacheHelperTest {

    private lateinit var cacheHelper: CacheHelper
    private lateinit var clock: FakeClock
    private lateinit var key: String
    private var window: Duration = Duration.ZERO

    @Before
    fun setup() {
        clock = FakeClock()
        val context = ApplicationProvider.getApplicationContext<Application>()
        val prefs = StorageModule.provideCacheHelperSharedPreferences(context)
        cacheHelper = CacheHelper(prefs, clock)
    }

    @After
    fun cleanup() {
        cacheHelper.clear()
    }

    @Test
    fun testNormal() {
        key = "key"
        window = 2.days

        checkConstantCases()
        assertFalse(cacheHelper.isCached(key))
        assertTrue(cacheHelper.isExpired(key, window))
        assertTrue(cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window))
        assertFalse(cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window))

        cacheHelper.setCachedNow(key)

        checkConstantCases()
        assertTrue(cacheHelper.isCached(key))
        assertFalse(cacheHelper.isExpired(key, window))
        assertFalse(cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window))
        assertTrue(cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window))

        clock.offset(window - 1.milliseconds)

        checkConstantCases()
        assertTrue(cacheHelper.isCached(key))
        assertFalse(cacheHelper.isExpired(key, window))
        assertFalse(cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window))
        assertTrue(cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window))

        clock.offset(1.milliseconds)

        checkConstantCases()
        assertTrue(cacheHelper.isCached(key))
        assertTrue(cacheHelper.isExpired(key, window))
        assertTrue(cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window))
        assertFalse(cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window))

        cacheHelper.remove(key)

        checkConstantCases()
        assertFalse(cacheHelper.isCached(key))
        assertTrue(cacheHelper.isExpired(key, window))
        assertTrue(cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window))
        assertFalse(cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window))
    }

    private fun checkConstantCases() {
        assertFalse(cacheHelper.shouldUpdate(CachePolicy.Cache, key, window))
        assertTrue(cacheHelper.shouldUpdate(CachePolicy.Remote, key, window))
        assertTrue(cacheHelper.shouldUseCache(CachePolicy.Cache, key, window))
        assertFalse(cacheHelper.shouldUseCache(CachePolicy.Remote, key, window))
    }

    @Test
    fun testObserveCacheUpdates() = runBlockingTest {
        key = "key"

        val actualValues = mutableListOf<Long>()
        val expectedValues = mutableListOf<Long>()
        val job = launch {
            cacheHelper.observeCacheUpdates(key).collect { actualValues.add(it) }
        }

        expectedValues.add(0L)
        assertEquals(actualValues, expectedValues)

        cacheHelper.setCachedNow(key)
        expectedValues.add(clock.currentTimeMillis())
        assertEquals(actualValues, expectedValues)

        cacheHelper.setCachedNow(key)
        assertEquals(actualValues, expectedValues)

        clock.offset(24.minutes)
        cacheHelper.setCachedNow(key)
        expectedValues.add(clock.currentTimeMillis())
        assertEquals(actualValues, expectedValues)

        clock.offset(1.minutes)
        cacheHelper.remove(key)
        expectedValues.add(0L)
        assertEquals(actualValues, expectedValues)

        job.cancel()
    }

    class FakeClock : Clock {
        private var current: Long = 0

        init {
            setToSystem()
        }

        override fun currentTimeMillis() = current

        fun setToSystem() {
            current = System.currentTimeMillis()
        }

        fun offset(offset: Duration) {
            current += offset.toLongMilliseconds()
        }
    }
}