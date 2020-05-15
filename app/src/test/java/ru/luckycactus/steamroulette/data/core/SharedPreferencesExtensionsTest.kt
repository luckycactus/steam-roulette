package ru.luckycactus.steamroulette.data.core

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.properties.ReadWriteProperty


@RunWith(AndroidJUnit4::class)
class SharedPreferencesExtensionsTest {

    lateinit var prefs: SharedPreferences
    private val key = "test"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        prefs = context.getSharedPreferences("shared_prefs_extensions_test", Context.MODE_PRIVATE)
    }

    @Test
    fun testIntPreference() = testPreference(
        prefs.int(key, -1),
        prefs.intFlow(key, -1),
        prefs.intLiveData(key, -1),
        listOf(-1, 2, 0)
    )

    @Test
    fun testLongPreference() = testPreference(
        prefs.long(key, -1L),
        prefs.longFlow(key, -1L),
        prefs.longLiveData(key, -1),
        listOf(-1L, 2L, 0)
    )

    @Test
    fun testFloatPreference() = testPreference(
        prefs.float(key, -1.5f),
        prefs.floatFlow(key, -1.5f),
        prefs.floatLiveData(key, -1.5f),
        listOf(-1.5f, 2.345f, 0f)
    )

    @Test
    fun testBooleanPreference() = testPreference(
        prefs.boolean(key, true),
        prefs.booleanFlow(key, true),
        prefs.booleanLiveData(key, true),
        listOf(true, false)
    )

    @Test
    fun testStringPreference() = testPreference(
        prefs.string(key, "default"),
        prefs.stringFlow(key, "default"),
        prefs.stringLiveData(key, "default"),
        listOf("default", "1")
    )

    @Test
    fun testStringPreferenceNullDefault() = testPreference(
        prefs.string(key, null),
        prefs.stringFlow(key, null),
        prefs.stringLiveData(key, null),
        listOf(null, "1")
    )

    private fun <T> testPreference(
        delegate: ReadWriteProperty<Any, T>,
        flow: Flow<T>,
        liveData: LiveData<T>,
        values: List<T>
    ) = runBlockingTest {
        object {
            var pref by delegate
        }.run {
            val actualFlowValues = mutableListOf<T>()
            val actualLiveDataValues = mutableListOf<T>()
            val job = launch {
                flow.collect { actualFlowValues += it }
            }
            val liveDataObserver = Observer<T> { actualLiveDataValues += it }
            liveData.observeForever(liveDataObserver)

            try {
                for ((index, value) in values.withIndex()) {
                    if (index > 0) {
                        pref = value
                    }
                    assertEquals(pref, value)
                    assertEquals(actualFlowValues, values.subList(0, index + 1))
                    assertEquals(actualLiveDataValues, values.subList(0, index + 1))
                }

                //check flow and livedata don't emit duplicates
                pref = values.last()
                assertEquals(actualFlowValues, values)
                assertEquals(actualLiveDataValues, values)

                prefs.edit { remove(key) }
                assertEquals(pref, values[0])
                assertEquals(actualFlowValues.last(), values[0])
                assertEquals(actualLiveDataValues.last(), values[0])
            } finally {
                liveData.removeObserver(liveDataObserver)
                job.cancel()
            }
        }
    }

    @After
    fun tearDown() {
        prefs.edit { clear() }
    }
}