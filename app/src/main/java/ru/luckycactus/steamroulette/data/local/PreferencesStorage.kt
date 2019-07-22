package ru.luckycactus.steamroulette.data.local

//todo nullable
interface PreferencesStorage {

    operator fun set(key: String, value: Int)

    operator fun set(key: String, value: Long)

    operator fun set(key: String, value: Float)

    operator fun set(key: String, value: Boolean)

    operator fun set(key: String, value: String)

    fun remove(key: String)

    fun getInt(key: String, defaultValue: Int = 0): Int

    fun getLong(key: String, defaultValue: Long = 0L): Long

    fun getFloat(key: String, defaultValue: Float = 0f): Float

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    fun getString(key: String, defaultValue: String? = null): String?

    fun contains(key: String): Boolean
}

//todo нужно ли?
@Throws(ClassCastException::class)
inline operator fun <reified T : Any> PreferencesStorage.get(key: String, defaultValue: T): T {
    return when (T::class) {
        Int::class -> getInt(key, defaultValue as Int) as T
        Long::class -> getLong(key, defaultValue as Long) as T
        Float::class -> getFloat(key, defaultValue as Float) as T
        Boolean::class -> getBoolean(key, defaultValue as Boolean) as T
        String::class -> getString(key, defaultValue as String) as T
        else -> throw ClassCastException("Unsupported preference type: ${T::class}")
    }
}


@Throws(ClassCastException::class)
inline operator fun <reified T : Any> PreferencesStorage.get(key: String): T {
    return when (T::class) {
        Int::class -> getInt(key) as T
        Long::class -> getLong(key) as T
        Float::class -> getFloat(key) as T
        Boolean::class -> getBoolean(key) as T
        String::class -> getString(key) as T
        else -> throw ClassCastException("Unsupported preference type: ${T::class}")
    }
}