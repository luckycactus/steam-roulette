package ru.luckycactus.steamroulette.data.local

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesStorage constructor(
    context: Context,
    prefsName: String
) : PreferencesStorage {

    private val preferences =
        context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun set(key: String, value: Int) {
        preferences.edit { putInt(key, value) }
    }

    override fun set(key: String, value: Long) {
        preferences.edit { putLong(key, value) }
    }

    override fun set(key: String, value: Float) {
        preferences.edit { putFloat(key, value) }
    }

    override fun set(key: String, value: Boolean) {
        preferences.edit { putBoolean(key, value) }
    }

    override fun set(key: String, value: String) {
        preferences.edit { putString(key, value) }
    }

    override fun remove(key: String) {
        preferences.edit { remove(key) }
    }

    override fun getInt(key: String, defaultValue: Int) = preferences.getInt(key, defaultValue)

    override fun getLong(key: String, defaultValue: Long) = preferences.getLong(key, defaultValue)

    override fun getFloat(key: String, defaultValue: Float) = preferences.getFloat(key, defaultValue)

    override fun getBoolean(key: String, defaultValue: Boolean) = preferences.getBoolean(key, defaultValue)

    override fun getString(key: String, defaultValue: String?): String? = preferences.getString(key, defaultValue)

    override fun contains(key: String): Boolean {
        return preferences.contains(key)
    }
}