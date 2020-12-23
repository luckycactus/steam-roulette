package ru.luckycactus.steamroulette.data.core

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
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
    fun while_before_insert__should_not_be_cached_and_valid() {
        checkCacheState(shouldBeCached = false, shouldBeValid = false)
    }

    @Test
    fun while_just_inserted__should_be_valid(): Unit = runBlocking {
        cacheHelper.setCachedNow(key)
        checkCacheState(shouldBeCached = true, shouldBeValid = true)
    }

    @Test
    fun while_right_before_window_end__should_be_valid(): Unit = runBlocking {
        cacheHelper.setCachedNow(key)
        clock.advanceTimeBy(window - 1.milliseconds)
        checkCacheState(shouldBeCached = true, shouldBeValid = true)
    }

    @Test
    fun while_right_after_window_end__should_be_invalid(): Unit = runBlocking {
        cacheHelper.setCachedNow(key)
        clock.advanceTimeBy(window)
        checkCacheState(shouldBeCached = true, shouldBeValid = false)
    }

    @Test
    fun when_remove__should_be_invalid_and_not_exist(): Unit = runBlocking {
        cacheHelper.setCachedNow(key)
        cacheHelper.remove(key)
        checkCacheState(shouldBeCached = false, shouldBeValid = false)
    }

    private fun checkCacheState(shouldBeCached: Boolean, shouldBeValid: Boolean) = runBlocking {
        checkConstantCases()
        assertThat(cacheHelper.isCached(key)).isEqualTo(shouldBeCached)
        assertThat(cacheHelper.isExpired(key, window)).isEqualTo(!shouldBeValid)
        assertThat(
            cacheHelper.shouldUpdate(CachePolicy.CacheOrRemote, key, window)
        ).isEqualTo(!shouldBeValid)
        assertThat(
            cacheHelper.shouldUseCache(CachePolicy.CacheOrRemote, key, window)
        ).isEqualTo(shouldBeValid)
    }

    private suspend fun checkConstantCases() {
        assertThat(cacheHelper.shouldUpdate(CachePolicy.Cache, key, window)).isFalse()
        assertThat(cacheHelper.shouldUpdate(CachePolicy.Remote, key, window)).isTrue()
        assertThat(cacheHelper.shouldUseCache(CachePolicy.Cache, key, window)).isTrue()
        assertThat(cacheHelper.shouldUseCache(CachePolicy.Remote, key, window)).isFalse()
    }

    @Test
    fun when_observeCacheUpdates__while_not_cached__should_emit_zero() = runBlocking {
        testFlow(cacheHelper.observeCacheUpdates(key)) {
            assertNextValue(0L)
        }
    }

    @Test
    fun when_observeCacheUpdates__while_after_cache_insert__should_emit_value() = runBlocking {
        testFlow(cacheHelper.observeCacheUpdates(key)) {
            nextValue()
            cacheHelper.setCachedNow(key)
            assertNextValue(clock.currentTimeMillis())
        }
    }


    @Test
    fun when_observeCacheUpdates__should_emit_distinct_values_until_changed() = runBlocking {
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
    fun when_observeCacheUpdates__while_cache_removed__should_emit_zero() = runBlocking {
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