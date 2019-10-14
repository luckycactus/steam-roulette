package ru.luckycactus.steamroulette.data.user.datastore

import android.content.SharedPreferences
import android.system.Os
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.DB
import ru.luckycactus.steamroulette.data.long
import ru.luckycactus.steamroulette.data.longLiveData
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.data.stringLiveData
import ru.luckycactus.steamroulette.data.user.UserRepositoryImpl

class LocalUserDataStore(
    private val db: DB,
    private val userPreferences: SharedPreferences
) : UserDataStore.Local {

    private var currentUserSteam64Pref by userPreferences.long(SIGNED_USER_KEY, 0)

    override suspend fun getUserSummary(steam64: Long): UserSummaryEntity =
        db.userSummaryDao().getUserSummary(steam64)

    override suspend fun saveUserSummaryToCache(userSummary: UserSummaryEntity) {
        db.userSummaryDao().saveUserSummaryToCache(userSummary)
    }

    override fun observeUserSummary(steam64: Long): LiveData<UserSummaryEntity> =
        db.userSummaryDao().observeUserSummary(steam64)

    override fun saveSignedInUser(steam64: Long) {
        currentUserSteam64Pref = steam64
    }

    override fun getCurrentUserSteam64(): Long = currentUserSteam64Pref

    override fun observeCurrentUserSteam64(): LiveData<Long> =
        userPreferences.longLiveData(SIGNED_USER_KEY, 0)

    override fun removeCurrentUserSteam64() {
        userPreferences.edit { remove(SIGNED_USER_KEY) }
    }

    companion object {
        const val SIGNED_USER_KEY = "signed_user_key"
    }
}