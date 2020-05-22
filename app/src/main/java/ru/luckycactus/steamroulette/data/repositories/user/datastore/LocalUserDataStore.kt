package ru.luckycactus.steamroulette.data.repositories.user.datastore

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.core.long
import ru.luckycactus.steamroulette.data.core.longFlow
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class LocalUserDataStore @Inject constructor(
    private val db: AppDatabase,
    @Identified(R.id.userCachePrefs) private val userPreferences: SharedPreferences
) : UserDataStore.Local {

    private var currentUserSteam64Pref by userPreferences.long(
        CURRENT_USER_KEY,
        CURRENT_USER_DEFAULT_VALUE
    )

    override val currentUserSteamIdFlow = userPreferences.longFlow(
        CURRENT_USER_KEY,
        CURRENT_USER_DEFAULT_VALUE
    ).map { fromSteam64(it) }

    override suspend fun getUserSummary(steamId: SteamId): UserSummaryEntity =
        db.userSummaryDao().get(steamId.asSteam64())

    override suspend fun saveUserSummary(userSummary: UserSummaryEntity) {
        db.userSummaryDao().upsert(userSummary)
    }

    override suspend fun removeUserSummary(steamId: SteamId) {
        db.userSummaryDao().delete(steamId.asSteam64())
    }

    override fun observeUserSummary(steamId: SteamId): Flow<UserSummaryEntity> =
        db.userSummaryDao().observe(steamId.asSteam64()).distinctUntilChanged().filterNotNull()

    override fun setCurrentUser(steamId: SteamId) {
        currentUserSteam64Pref = steamId.asSteam64()
    }

    override fun getCurrentUserSteam64(): SteamId? = fromSteam64(currentUserSteam64Pref)

    override fun removeCurrentUserSteamId() {
        userPreferences.edit { remove(CURRENT_USER_KEY) }
    }

    private fun fromSteam64(steam64: Long) =
        if (steam64 == 0L) null else SteamId.fromSteam64(steam64)

    companion object {
        const val CURRENT_USER_KEY = "signed_user_key"
        const val CURRENT_USER_DEFAULT_VALUE = 0L
    }
}