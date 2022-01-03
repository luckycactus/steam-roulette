package ru.luckycactus.steamroulette.data.core

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import com.squareup.moshi.Moshi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadOnlyProperty
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

fun SharedPreferences.int(default: Int = 0, key: String?) =
    PreferenceDelegate(Int::class.java, this, key, default)

fun SharedPreferences.long(default: Long = 0, key: String?) =
    PreferenceDelegate(Long::class.java, this, key, default)

fun SharedPreferences.float(default: Float = 0f, key: String?) =
    PreferenceDelegate(Float::class.java, this, key, default)

fun SharedPreferences.boolean(default: Boolean = false, key: String?) =
    PreferenceDelegate(Boolean::class.java, this, key, default)

fun SharedPreferences.string(default: String = "", key: String?) =
    PreferenceDelegate(String::class.java, this, key, default)

inline fun <reified T> SharedPreferences.type(default: T, key: String?) =
    PreferenceDelegate(T::class.java, this, key, default)


fun SharedPreferences.intLiveData(default: Int, key: String) =
    liveData(Int::class.java, key, default)

fun SharedPreferences.longLiveData(default: Long, key: String) =
    liveData(Long::class.java, key, default)

fun SharedPreferences.floatLiveData(default: Float, key: String) =
    liveData(Float::class.java, key, default)

fun SharedPreferences.booleanLiveData(default: Boolean, key: String) =
    liveData(Boolean::class.java, key, default)

fun SharedPreferences.stringLiveData(default: String, key: String): LiveData<String> =
    liveData(String::class.java, key, default)

fun <T : Any> SharedPreferences.typeLiveData(
    clazz: Class<T>,
    default: T,
    key: String
): LiveData<T> = liveData(clazz, key, default)


fun SharedPreferences.intFlow(default: Int, key: String) =
    flow(Int::class.java, key, default)

fun SharedPreferences.longFlow(default: Long, key: String) =
    flow(Long::class.java, key, default)

fun SharedPreferences.floatFlow(default: Float, key: String) =
    flow(Float::class.java, key, default)

fun SharedPreferences.booleanFlow(default: Boolean, key: String) =
    flow(Boolean::class.java, key, default)

fun SharedPreferences.stringFlow(default: String, key: String) =
    flow(String::class.java, key, default)

fun <T : Any> SharedPreferences.typeFlow(clazz: Class<T>, default: T, key: String) =
    flow(clazz, key, default)


fun SharedPreferences.intMultiPref(mapKey: String?) =
    MultiPreferenceDelegate(mapKey) { key ->
        MultiPreference(Int::class.java, this, key)
    }

fun SharedPreferences.longMultiPref(mapKey: String?) =
    MultiPreferenceDelegate(mapKey) { key ->
        MultiPreference(Long::class.java, this, key)
    }

fun SharedPreferences.floatMultiPref(mapKey: String?) =
    MultiPreferenceDelegate(mapKey) { key ->
        MultiPreference(Float::class.java, this, key)
    }

fun SharedPreferences.booleanMultiPref(mapKey: String?) =
    MultiPreferenceDelegate(mapKey) { key ->
        MultiPreference(Boolean::class.java, this, key)
    }

fun SharedPreferences.stringMultiPref(mapKey: String?) =
    MultiPreferenceDelegate(mapKey) { key ->
        MultiPreference(String::class.java, this, key)
    }

inline fun <reified T : Any> SharedPreferences.typeMultiPref(mapKey: String?) =
    MultiPreferenceDelegate(mapKey) { key ->
        MultiPreference(T::class.java, this, key)
    }

class PreferenceDelegate<T> constructor(
    private val clazz: Class<T>,
    private val prefs: SharedPreferences,
    private val key: String? = null,
    private val default: T
) : ReadWriteProperty<Any, T> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        prefs.set(clazz, key(property), value)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return prefs.get(clazz, key(property), default)
    }

    private fun key(property: KProperty<*>) = key ?: property.name
}

private fun <T> SharedPreferences.liveData(
    clazz: Class<T>,
    key: String,
    default: T
): LiveData<T> = SharedPreferenceLiveData(clazz, this, key, default)

private class SharedPreferenceLiveData<T>(
    clazz: Class<T>,
    prefs: SharedPreferences,
    private val key: String,
    default: T
) : LiveData<T>() {

    private val compositeListener = prefs.getCompositeListener()
    private val prefValue by PreferenceDelegate(clazz, prefs, key, default)
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

private fun <T> SharedPreferences.flow(
    clazz: Class<T>,
    key: String,
    default: T,
) = callbackFlow {
    val prefHolder = object : Any() {
        val prefValue by PreferenceDelegate(clazz, this@flow, key, default)
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

class MultiPreferenceDelegate<T>(
    private val multiKey: String? = null,
    private val createMultiPreference: (key: String) -> MultiPreference<T>
) : ReadOnlyProperty<Any, MultiPreference<T>> {

    private var value: MultiPreference<T>? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): MultiPreference<T> {
        var value = this.value
        if (value == null) {
            val key = multiKey ?: property.name
            value = createMultiPreference(key)
            this.value = value
        }
        return value
    }
}

class MultiPreference<T>(
    private val clazz: Class<T>,
    private val prefs: SharedPreferences,
    private val mapKey: String
) {
    operator fun set(key: String, value: T) {
        prefs.set(clazz, keyFor(key), value)
    }

    operator fun get(key: String, default: T): T {
        return prefs.get(clazz, keyFor(key), default)
    }

    fun remove(key: String) {
        prefs.edit {
            remove(keyFor(key))
        }
    }

    fun flow(key: String, default: T) =
        prefs.flow(clazz, keyFor(key), default)

    operator fun set(key: Any, value: T) = set(key.toString(), value)

    operator fun get(key: Any, default: T): T = get(key.toString(), default)

    fun remove(key: Any) = remove(key.toString())

    fun flow(key: Any, default: T) = flow(key.toString(), default)

    fun keyFor(entryKey: String) = "$mapKey-$entryKey"

    fun keyFor(entryKey: Any) = keyFor(entryKey.toString())
}

private typealias PrefChangeListener = () -> Unit

private fun SharedPreferences.getCompositeListener() =
    compositeListeners.getOrPut(this) {
        CompositePreferenceChangeListener().also {
            registerOnSharedPreferenceChangeListener(it)
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

private fun <T> SharedPreferences.get(clazz: Class<T>, key: String, default: T?): T {
    return when (clazz) {
        Boolean::class.java -> getBoolean(key, default as Boolean)
        Int::class.java -> getInt(key, default as Int)
        Float::class.java -> getFloat(key, default as Float)
        Long::class.java -> getLong(key, default as Long)
        String::class.java -> getString(key, default as String?)
        else -> getString(key, null)?.let { SharedPreferencesConfig.moshi.adapter(clazz).fromJson(it) } ?: default
    } as T
}

private fun <T> SharedPreferences.set(clazz: Class<T>, key: String, value: T) {
    with(edit()) {
        when (clazz) {
            Boolean::class.java -> putBoolean(key, value as Boolean)
            Int::class.java -> putInt(key, value as Int)
            Float::class.java -> putFloat(key, value as Float)
            Long::class.java -> putLong(key, value as Long)
            String::class.java -> putString(key, value as String)
            else -> {
                val json = value?.let { SharedPreferencesConfig.moshi.adapter(clazz).toJson(it) }
                putString(key, json)
            }
        }
        apply()
    }
}