package ru.luckycactus.steamroulette.data.user.datastore

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.long
import ru.luckycactus.steamroulette.data.longLiveData
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

class LocalUserDataStore(
    private val db: DB,
    private val userPreferences: SharedPreferences
) : UserDataStore.Local {

    private var currentUserSteam64Pref by userPreferences.long(
        CURRENT_USER_KEY,
        CURRENT_USER_DEFAULT_VALUE
    )

    private var currentUserSteam64LiveData = userPreferences.longLiveData(
        CURRENT_USER_KEY,
        CURRENT_USER_DEFAULT_VALUE
    )

    override suspend fun getUserSummary(steam64: Long): UserSummaryEntity =
        db.userSummaryDao().getUserSummary(steam64)

    override suspend fun saveUserSummary(userSummary: UserSummaryEntity) {
        db.userSummaryDao().insertUserSummary(userSummary)
    }

    override suspend fun removeUserSummary(steam64: Long) {
        db.userSummaryDao().removeUserSummary(steam64)
    }

    override fun observeUserSummary(steam64: Long): LiveData<UserSummaryEntity> =
        db.userSummaryDao().observeUserSummary(steam64).distinctUntilChanged()

    override fun setCurrentUser(steam64: Long) {
        currentUserSteam64Pref = steam64
    }

    override fun getCurrentUserSteam64(): Long = currentUserSteam64Pref

    override fun observeCurrentUserSteam64(): LiveData<Long> =
        currentUserSteam64LiveData

    override fun removeCurrentUserSteam64() {
        userPreferences.edit { remove(CURRENT_USER_KEY) }
    }

    companion object {
        const val CURRENT_USER_KEY = "signed_user_key"
        const val CURRENT_USER_DEFAULT_VALUE = 0L
    }
}