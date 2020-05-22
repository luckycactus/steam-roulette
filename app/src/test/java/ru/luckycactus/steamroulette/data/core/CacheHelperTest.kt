package ru.luckycactus.steamroulette.data.core

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.di.common.StorageModule
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.test.util.fakes.FakeClock
import ru.luckycactus.steamroulette.test.util.checkValues
import kotlin.time.days
import kotlin.time.milliseconds
import kotlin.time.minutes

@RunWith(AndroidJUnit4::class)
class CacheHelperTest {

    private lateinit var cacheHelper: CacheHelper
    private lateinit var clock: FakeClock
    private var key = "key"
    private var window = 2.days

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
    fun `cache should not be cached and valid before insert`() {
        checkCacheState(shouldBeCached = false, shouldBeValid = false)
    }

    @Test
    fun `cache should be valid if just inserted`() {
        cacheHelper.setCachedNow(key)
        checkCacheState(shouldBeCached = true, shouldBeValid = true)
    }

    @Test
    fun `cache should be valid right before window end`() {
        cacheHelper.setCachedNow(key)
        clock.advanceTimeBy(window - 1.milliseconds)
        checkCacheState(shouldBeCached = true, shouldBeValid = true)
    }

    @Test
    fun `cache should be invalid after window end`() {
        cacheHelper.setCachedNow(key)
        clock.advanceTimeBy(window)
        checkCacheState(shouldBeCached = true, shouldBeValid = false)
    }

    @Test
    fun `cache should be invalid and not exist after remove`() {
        cacheHelper.setCachedNow(key)
        cacheHelper.remove(key)
        checkCacheState(shouldBeCached = false, shouldBeValid = false)
    }

    private fun checkCacheState(shouldBeCached: Boolean, shouldBeValid: Boolean) {
        checkConstantCases()
        assertEquals(cacheHelper.isCached(key), shouldBeCached)
        assertEquals(cacheHelper.isExpired(key, window), !shouldBeValid)
        assertEquals(
            cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window),
            !shouldBeValid
        )
        assertEquals(
            cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window),
            shouldBeValid
        )
    }

    private fun checkConstantCases() {
        assertFalse(cacheHelper.shouldUpdate(CachePolicy.Cache, key, window))
        assertTrue(cacheHelper.shouldUpdate(CachePolicy.Remote, key, window))
        assertTrue(cacheHelper.shouldUseCache(CachePolicy.Cache, key, window))
        assertFalse(cacheHelper.shouldUseCache(CachePolicy.Remote, key, window))
    }

    @Test
    fun `obsereCacheUpdates() should emit zero if not cached`() =
        cacheHelper.observeCacheUpdates(key).checkValues(listOf(0L)) {}

    @Test
    fun `obsereCacheUpdates() should emit value after cache insert`() {
        val expectedValues = listOf(0L, clock.currentTimeMillis())
        cacheHelper.observeCacheUpdates(key).checkValues(expectedValues) {
            cacheHelper.setCachedNow(key)
        }
    }

    @Test
    fun `obsereCacheUpdates() should emit distinct values until changed`() {
        val expectedValues = mutableListOf(0L)
        cacheHelper.observeCacheUpdates(key).checkValues(expectedValues) {
            cacheHelper.setCachedNow(key)
            cacheHelper.setCachedNow(key)
            expectedValues += clock.currentTimeMillis()

            clock.advanceTimeBy(1.days)
            cacheHelper.setCachedNow(key)
            expectedValues += clock.currentTimeMillis()
        }
    }

    @Test
    fun `obsereCacheUpdates() should emit zero if cache removed`() {
        val expectedValues = mutableListOf(0L)
        cacheHelper.observeCacheUpdates(key).checkValues(expectedValues) {
            cacheHelper.setCachedNow(key)
            expectedValues += clock.currentTimeMillis()

            clock.advanceTimeBy(1.minutes)
            cacheHelper.remove(key)
            expectedValues += 0L
        }
    }
}