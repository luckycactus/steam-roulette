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
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.properties.ReadWriteProperty


@RunWith(AndroidJUnit4::class)
class SharedPreferencesExtensionsTest {

    //todo test customtypes and multiprefs

    lateinit var prefs: SharedPreferences
    private val key = "test"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        prefs = context.getSharedPreferences("shared_prefs_extensions_test", Context.MODE_PRIVATE)
    }

    @After
    fun tearDown() {
        prefs.edit { clear() }
    }

    @Test
    fun testIntPreference() = testPreference(
        prefs.int(-1, key),
        prefs.intFlow(-1, key),
        prefs.intLiveData(-1, key),
        listOf(-1, 2, 0)
    )

    @Test
    fun testLongPreference() = testPreference(
        prefs.long(-1L, key),
        prefs.longFlow(-1L, key),
        prefs.longLiveData(-1, key),
        listOf(-1L, 2L, 0)
    )

    @Test
    fun testFloatPreference() = testPreference(
        prefs.float(-1.5f, key),
        prefs.floatFlow(-1.5f, key),
        prefs.floatLiveData(-1.5f, key),
        listOf(-1.5f, 2.345f, 0f)
    )

    @Test
    fun testBooleanPreference() = testPreference(
        prefs.boolean(true, key),
        prefs.booleanFlow(true, key),
        prefs.booleanLiveData(true, key),
        listOf(true, false)
    )

    @Test
    fun testStringPreference() = testPreference(
        prefs.string("default", key),
        prefs.stringFlow("default", key),
        prefs.stringLiveData("default", key),
        listOf("default", "1")
    )

    @Test
    fun testStringPreferenceNullDefault() = testPreference(
        prefs.string("null", key),
        prefs.stringFlow("null", key),
        prefs.stringLiveData("null", key),
        listOf("null", "1")
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
                    assertThat(pref).isEqualTo(value)
                    assertThat(actualFlowValues).isEqualTo(values.subList(0, index + 1))
                    assertThat(actualLiveDataValues).isEqualTo(values.subList(0, index + 1))
                }

                //check flow and livedata don't emit duplicates
                pref = values.last()
                assertThat(actualFlowValues).isEqualTo(values)
                assertThat(actualLiveDataValues).isEqualTo(values)

                prefs.edit { remove(key) }
                assertThat(pref).isEqualTo(values[0])
                assertThat(actualFlowValues.last()).isEqualTo(values[0])
                assertThat(actualLiveDataValues.last()).isEqualTo(values[0])
            } finally {
                liveData.removeObserver(liveDataObserver)
                job.cancel()
            }
        }
    }
}