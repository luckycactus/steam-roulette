package ru.luckycactus.steamroulette.data.user

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import java.util.concurrent.TimeUnit

class UserCacheImpl(
    private val prefs: PreferencesStorage,
    private val cacheHelper: CacheHelper,
    private val gson: Gson
) : UserCache {

    override fun putUserSummary(userSummary: UserSummaryEntity) {
        val json = gson.toJson(userSummary)
        prefs[userSummary.steam64] = json
        cacheHelper.setCachedNow(userSummary.steam64)
    }

    override fun getUserSummary(steam64: Long): UserSummaryEntity {
        val json = prefs.getString(steam64.toString())!!
        val type = object : TypeToken<UserSummaryEntity>() {}.type
        return gson.fromJson(json, type)
    }

    override fun isExpired(steam64: Long, window: Long, timeUnit: TimeUnit): Boolean =
        cacheHelper.isExpired(steam64.toString(), window, timeUnit)
}

interface UserCache {
    fun getUserSummary(steam64: Long): UserSummaryEntity

    fun putUserSummary(userSummary: UserSummaryEntity)

    fun isExpired(steam64: Long, window: Long, timeUnit: TimeUnit): Boolean
}