package ru.luckycactus.steamroulette.data.user.datastore

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.data.stringLiveData

//todo db?
class LocalUserDataStore(
    private val prefs: SharedPreferences,
    private val cacheHelper: CacheHelper,
    private val gson: Gson
) : UserDataStore.Local {

    override suspend fun getUserSummary(steam64: Long): UserSummaryEntity {
        return fromJson(prefs.getString(steam64.toString(), null)!!) //todo
    }

    override suspend fun saveUserSummaryToCache(userSummary: UserSummaryEntity) {
        val json = gson.toJson(userSummary)
        prefs.edit { putString(userSummary.steam64, json) }
        cacheHelper.setCachedNow(userSummary.steam64)
    }

    override fun observeUserSummary(steam64: Long): LiveData<UserSummaryEntity> {
        return prefs.stringLiveData(steam64.toString(), null).map {
            fromJson(it!!) //todo не эмитить nulls
        }
    }

    private fun fromJson(json: String): UserSummaryEntity {
        val type = object : TypeToken<UserSummaryEntity>() {}.type
        return gson.fromJson(json, type)
    }
}