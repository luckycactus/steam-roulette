package ru.luckycactus.steamroulette.data.repositories.user

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.core.long
import ru.luckycactus.steamroulette.data.core.longFlow
import ru.luckycactus.steamroulette.data.repositories.user.datasource.LocalUserDataSource
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserSessionRepositoryImpl @Inject constructor(
    @Named("user-cache") private val userPreferences: SharedPreferences
) : UserSessionRepository {
    private var currentUserPref by userPreferences.long(
        LocalUserDataSource.CURRENT_USER_KEY,
        LocalUserDataSource.CURRENT_USER_DEFAULT_VALUE
    )

    private val currentUserFlow = userPreferences.longFlow(
        LocalUserDataSource.CURRENT_USER_KEY,
        LocalUserDataSource.CURRENT_USER_DEFAULT_VALUE
    ).map { fromSteam64(it) }

    override val currentUser: SteamId?
        get() = fromSteam64(currentUserPref)

    override fun setCurrentUser(steamId: SteamId) {
        currentUserPref = steamId.as64()
    }

    override fun observeCurrentUser(): Flow<SteamId?> =
        currentUserFlow

    override fun isUserLoggedIn(): Boolean = currentUser != null

    override suspend fun logOut() {
        userPreferences.edit { remove(LocalUserDataSource.CURRENT_USER_KEY) }
    }

    private fun fromSteam64(steam64: Long) =
        if (steam64 == 0L) null else SteamId.fromSteam64(steam64)
}