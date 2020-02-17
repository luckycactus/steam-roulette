package ru.luckycactus.steamroulette.data.core

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val compositeListeners = WeakHashMap<SharedPreferences, CompositePreferenceChangeListener>()

private val SharedPreferences.compositeListener
    @Synchronized
    get() = compositeListeners.getOrPut(this, {
        CompositePreferenceChangeListener().also {
            registerOnSharedPreferenceChangeListener(it)
        }
    })

fun SharedPreferences.Editor.apply(block: SharedPreferences.Editor.() -> Unit) {
    block()
    apply()
}

fun SharedPreferences.Editor.commit(block: SharedPreferences.Editor.() -> Unit) {
    block()
    commit()
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
    liveData(Int::class.java, key, defValue)

fun SharedPreferences.longLiveData(key: String, defValue: Long) =
    liveData(Long::class.java, key, defValue)

fun SharedPreferences.floatLiveData(key: String, defValue: Float) =
    liveData(Float::class.java, key, defValue)

fun SharedPreferences.booleanLiveData(key: String, defValue: Boolean) =
    liveData(Boolean::class.java, key, defValue)

fun SharedPreferences.stringLiveData(key: String, defValue: String?): LiveData<String?> =
    SharedPreferenceLiveData(
        this,
        key,
        StringPreference(
            this,
            key,
            defValue
        )
    )


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

private fun <T> SharedPreferences.liveData(
    clazz: Class<T>,
    key: String,
    defValue: T
): LiveData<T> =
    SharedPreferenceLiveData(
        this,
        key,
        getDelegate(clazz, key, defValue)
    )

private fun <T> SharedPreferences.getDelegate(
    clazz: Class<T>,
    key: String,
    defValue: T
) = when (clazz) {
    Int::class.java -> IntPreference(
        this,
        key,
        defValue as Int
    )
    Long::class.java -> LongPreference(
        this,
        key,
        defValue as Long
    )
    Float::class.java -> FloatPreference(
        this,
        key,
        defValue as Float
    )
    Boolean::class.java -> BooleanPreference(
        this,
        key,
        defValue as Boolean
    )
    String::class.java -> StringPreference(
        this,
        key,
        defValue as String?
    )
    else -> throw Exception()
} as ReadWriteProperty<Any, T>

private class SharedPreferenceLiveData<T>(
    prefs: SharedPreferences,
    private val key: String,
    delegate: ReadWriteProperty<Any, T>
) : LiveData<T>() {

    private val compositeListener = prefs.compositeListener
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
    private val listeners = mutableMapOf<String, MutableSet<() -> Unit>>()

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String
    ) {
        listeners[key]?.forEach { it.invoke() }
    }

    fun addListener(key: String, listener: () -> Unit) {
        synchronized(key.intern()) {
            listeners.getOrPut(
                key,
                { Collections.newSetFromMap<() -> Unit>(WeakHashMap<() -> Unit, Boolean>()) }
            ).add(listener)
        }
    }

    fun removeListener(key: String, listener: () -> Unit) {
        listeners[key]?.remove(listener)
    }
}