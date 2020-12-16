package ru.luckycactus.steamroulette.data.core

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.util.fakes.FakeClock
import ru.luckycactus.steamroulette.util.testFlow
import kotlin.time.days
import kotlin.time.milliseconds
import kotlin.time.minutes

@RunWith(AndroidJUnit4::class)
class RoomCacheHelperTest {

    private lateinit var cacheHelper: RoomCacheHelper
    private lateinit var clock: FakeClock
    private lateinit var db: AppDatabase
    private val key = "key"
    private val window = 2.days

    @Before
    fun setup() {
        clock = FakeClock()
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = AppDatabase.buildTestDatabase(context)
        cacheHelper = RoomCacheHelper(db.cacheInfoDao(), clock)
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun cache_should_not_be_cached_and_valid_before_insert() {
        checkCacheState(shouldBeCached = false, shouldBeValid = false)
    }

    @Test
    fun cache_should_be_valid_if_just_inserted() = runBlocking {
        cacheHelper.setCachedNow(key)
        checkCacheState(shouldBeCached = true, shouldBeValid = true)
    }

    @Test
    fun cache_should_be_valid_right_before_window_end() = runBlocking {
        cacheHelper.setCachedNow(key)
        clock.advanceTimeBy(window - 1.milliseconds)
        checkCacheState(shouldBeCached = true, shouldBeValid = true)
    }

    @Test
    fun cache_should_be_invalid_after_window_end() = runBlocking {
        cacheHelper.setCachedNow(key)
        clock.advanceTimeBy(window)
        checkCacheState(shouldBeCached = true, shouldBeValid = false)
    }

    @Test
    fun cache_should_be_invalid_and_not_exist_after_remove() = runBlocking {
        cacheHelper.setCachedNow(key)
        cacheHelper.remove(key)
        checkCacheState(shouldBeCached = false, shouldBeValid = false)
    }

    private fun checkCacheState(shouldBeCached: Boolean, shouldBeValid: Boolean) = runBlocking {
        checkConstantCases()
        Assert.assertEquals(cacheHelper.isCached(key), shouldBeCached)
        Assert.assertEquals(cacheHelper.isExpired(key, window), !shouldBeValid)
        Assert.assertEquals(
            cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window),
            !shouldBeValid
        )
        Assert.assertEquals(
            cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window),
            shouldBeValid
        )
    }

    private suspend fun checkConstantCases() {
        Assert.assertFalse(cacheHelper.shouldUpdate(CachePolicy.Cache, key, window))
        Assert.assertTrue(cacheHelper.shouldUpdate(CachePolicy.Remote, key, window))
        Assert.assertTrue(cacheHelper.shouldUseCache(CachePolicy.Cache, key, window))
        Assert.assertFalse(cacheHelper.shouldUseCache(CachePolicy.Remote, key, window))
    }

    @Test
    fun observeCacheUpdates_should_emit_zero_if_not_cached() = runBlocking {
        testFlow(cacheHelper.observeCacheUpdates(key)) {
            assertNextValue(0L)
        }
    }

    @Test
    fun observeCacheUpdates_should_emit_value_after_cache_insert() = runBlocking {
        testFlow(cacheHelper.observeCacheUpdates(key)) {
            nextValue()
            cacheHelper.setCachedNow(key)
            assertNextValue(clock.currentTimeMillis())
        }
    }


    @Test
    fun observeCacheUpdates_should_emit_distinct_values_until_changed() = runBlocking {
        testFlow(cacheHelper.observeCacheUpdates(key)) {
            nextValue()

            cacheHelper.setCachedNow(key)
            cacheHelper.setCachedNow(key)
            assertNextValue(clock.currentTimeMillis())

            clock.advanceTimeBy(1.days)
            cacheHelper.setCachedNow(key)

            assertNextValue(clock.currentTimeMillis())
            assertEmpty()
        }

    }

    @Test
    fun observeCacheUpdates_should_emit_zero_if_cache_removed() = runBlocking {
        testFlow(cacheHelper.observeCacheUpdates(key)) {
            nextValue()

            cacheHelper.setCachedNow(key)
            nextValue()

            clock.advanceTimeBy(1.minutes)
            cacheHelper.remove(key)
            assertNextValue(0L)
        }
    }
}