package ru.luckycactus.steamroulette.data.user.datastore

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

//todo db?
class LocalUserDataStore(
    private val prefs: PreferencesStorage,
    private val cacheHelper: CacheHelper,
    private val gson: Gson
) : UserDataStore.Local {

    override suspend fun getUserSummary(steam64: Long): UserSummaryEntity {
        val json = prefs.getString(steam64.toString())!!
        val type = object : TypeToken<UserSummaryEntity>() {}.type
        return gson.fromJson(json, type)
    }

    override suspend fun saveUserSummaryToCache(userSummary: UserSummaryEntity) {
        val json = gson.toJson(userSummary)
        prefs[userSummary.steam64] = json
        cacheHelper.setCachedNow(userSummary.steam64)
    }
}