package ru.luckycactus.steamroulette.data.core

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val compositeListeners = Collections.synchronizedMap(
    WeakHashMap<SharedPreferences, CompositePreferenceChangeListener>()
)

inline fun SharedPreferences.Editor.edit(
    commit: Boolean = false,
    block: SharedPreferences.Editor.() -> Unit
) {
    block()
    if (commit)
        commit()
    else
        apply()
}

class IntPreference(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: Int
) : ReadWriteProperty<Any, Int> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return prefs.getInt(key, defaultValue)
    }
}

class LongPreference(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: Long
) : ReadWriteProperty<Any, Long> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        prefs.edit { putLong(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return prefs.getLong(key, defaultValue)
    }
}

class BooleanPreference(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
}

class FloatPreference(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: Float
) : ReadWriteProperty<Any, Float> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
        prefs.edit { putFloat(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Float {
        return prefs.getFloat(key, defaultValue)
    }
}

class StringPreference(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: String?
) : ReadWriteProperty<Any, String?> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        prefs.edit { putString(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return prefs.getString(key, defaultValue)
    }
}


fun SharedPreferences.int(key: String, defValue: Int = 0) =
    IntPreference(this, key, defValue)

fun SharedPreferences.long(key: String, defValue: Long = 0) =
    LongPreference(this, key, defValue)

fun SharedPreferences.float(key: String, defValue: Float = 0f) =
    FloatPreference(this, key, defValue)

fun SharedPreferences.boolean(key: String, defValue: Boolean = false) =
    BooleanPreference(this, key, defValue)

fun SharedPreferences.string(key: String, defValue: String? = null) =
    StringPreference(this, key, defValue)


fun SharedPreferences.intLiveData(key: String, defValue: Int) =
    liveData(key, IntPreference(this, key, defValue))

fun SharedPreferences.longLiveData(key: String, defValue: Long) =
    liveData(key, LongPreference(this, key, defValue))

fun SharedPreferences.floatLiveData(key: String, defValue: Float) =
    liveData(key, FloatPreference(this, key, defValue))

fun SharedPreferences.booleanLiveData(key: String, defValue: Boolean) =
    liveData(key, BooleanPreference(this, key, defValue))

fun SharedPreferences.stringLiveData(key: String, defValue: String?): LiveData<String?> =
    liveData(key, StringPreference(this, key, defValue))


fun SharedPreferences.intFlow(key: String, defValue: Int) =
    flow(key, IntPreference(this, key, defValue))

fun SharedPreferences.longFlow(key: String, defValue: Long) =
    flow(key, LongPreference(this, key, defValue))

fun SharedPreferences.floatFlow(key: String, defValue: Float) =
    flow(key, FloatPreference(this, key, defValue)).cancellable()

fun SharedPreferences.booleanFlow(key: String, defValue: Boolean) =
    flow(key, BooleanPreference(this, key, defValue))

fun SharedPreferences.stringFlow(key: String, defValue: String?): Flow<String?> =
    flow(key, StringPreference(this, key, defValue))


private typealias PrefChangeListener = () -> Unit

private fun SharedPreferences.getCompositeListener() =
    compositeListeners.getOrPut(this) {
        CompositePreferenceChangeListener().also {
            registerOnSharedPreferenceChangeListener(it)
        }
    }

private fun <T> SharedPreferences.liveData(
    key: String,
    delegate: ReadWriteProperty<Any, T>
): LiveData<T> =
    SharedPreferenceLiveData(
        this,
        key,
        delegate
    )

private fun <T> SharedPreferences.flow(
    key: String,
    delegate: ReadWriteProperty<Any, T>
) = callbackFlow {
    val prefHolder = object : Any() {
        val prefValue by delegate
    }
    val compositeListener = getCompositeListener()
    val listener = {
        offer(prefHolder.prefValue)
        Unit
    }
    listener.invoke()
    compositeListener.addListener(key, listener)
    awaitClose { compositeListener.removeListener(key, listener) }
}

private class SharedPreferenceLiveData<T>(
    prefs: SharedPreferences,
    private val key: String,
    delegate: ReadWriteProperty<Any, T>
) : LiveData<T>() {

    private val compositeListener = prefs.getCompositeListener()
    private val prefValue by delegate
    private val listener = { updateValue() }

    private var firstTime = true

    init {
        updateValue()
    }

    override fun onActive() {
        updateValue()
        compositeListener.addListener(key, listener)
    }

    override fun onInactive() {
        compositeListener.removeListener(key, listener)
    }

    private fun updateValue() {
        val newValue = prefValue
        if (firstTime
            || value == null && newValue != null
            || value != null && newValue != value
        ) {
            firstTime = false
            value = newValue
        }
    }
}

private class CompositePreferenceChangeListener :
    SharedPreferences.OnSharedPreferenceChangeListener {
    val keyListenersMultimap = ConcurrentHashMap<String, MutableSet<PrefChangeListener>>()

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String
    ) {
        getListenersSetForKey(key)?.forEach { it.invoke() }
    }

    fun addListener(key: String, listener: PrefChangeListener) {
        val listeners = keyListenersMultimap.getOrPut(
            key,
            { Collections.newSetFromMap(WeakHashMap()) }
        )
        listeners.add(listener)
    }

    fun removeListener(key: String, listener: PrefChangeListener) {
        getListenersSetForKey(key)?.remove(listener)
    }

    private fun getListenersSetForKey(key: String) = keyListenersMultimap[key]
}