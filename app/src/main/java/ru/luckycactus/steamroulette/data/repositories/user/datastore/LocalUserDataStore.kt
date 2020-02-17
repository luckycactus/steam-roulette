package ru.luckycactus.steamroulette.data.repositories.user.datastore

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.core.long
import ru.luckycactus.steamroulette.data.core.longLiveData
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserDataStore @Inject constructor(
    private val db: DB,
    @Identified(R.id.userCachePrefs) private val userPreferences: SharedPreferences
) : UserDataStore.Local {

    private var currentUserSteam64Pref by userPreferences.long(
        CURRENT_USER_KEY,
        CURRENT_USER_DEFAULT_VALUE
    )

    private var currentUserSteam64LiveData = userPreferences.longLiveData(
        CURRENT_USER_KEY,
        CURRENT_USER_DEFAULT_VALUE
    )

    override suspend fun getUserSummary(steamId: SteamId): UserSummaryEntity =
        db.userSummaryDao().get(steamId.asSteam64())

    override suspend fun saveUserSummary(userSummary: UserSummaryEntity) {
        db.userSummaryDao().upsert(userSummary)
    }

    override suspend fun removeUserSummary(steamId: SteamId) {
        db.userSummaryDao().delete(steamId.asSteam64())
    }

    override fun observeUserSummary(steamId: SteamId): LiveData<UserSummaryEntity> =
        db.userSummaryDao().observe(steamId.asSteam64()).distinctUntilChanged()

    override fun setCurrentUser(steamId: SteamId) {
        currentUserSteam64Pref = steamId.asSteam64()
    }

    override fun getCurrentUserSteam64(): SteamId? = fromSteam64(currentUserSteam64Pref)

    override fun observeCurrentUserSteam64(): LiveData<SteamId?> =
        currentUserSteam64LiveData.map { fromSteam64(it) }

    override fun removeCurrentUserSteam64() {
        userPreferences.edit { remove(CURRENT_USER_KEY) }
    }

    private fun fromSteam64(steam64: Long) =
        if (steam64 == 0L) null else SteamId.fromSteam64(steam64)

    companion object {
        const val CURRENT_USER_KEY = "signed_user_key"
        const val CURRENT_USER_DEFAULT_VALUE = 0L
    }
}