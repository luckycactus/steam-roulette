package ru.luckycactus.steamroulette.data.user

import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.CachePolicy
import ru.luckycactus.steamroulette.domain.user.SteamId
import ru.luckycactus.steamroulette.domain.user.UserSummary

class UserRepositoryImpl(
    private val userDataStoreFactory: UserDataStoreFactory,
    private val mapper: UserSummaryMapper,
    private val userPreferences: PreferencesStorage //todo replace by room
) : UserRepository {
    //todo move to datastore?
    override fun saveSignedInUser(steamId: SteamId) {
        userPreferences[SIGNED_USER_KEY] = steamId.asSteam64()
    }

    override fun getSignedInUserSteamId(): SteamId =
        SteamId.fromSteam64(getSignedUserSteam64())

    override fun isUserSignedIn(): Boolean =
        userPreferences.getLong(SIGNED_USER_KEY) != 0L

    override suspend fun getUserSummary(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary {
        return getUserSummary(steamId.asSteam64(), cachePolicy)
    }

    override suspend fun getSignedInUserSummary(cachePolicy: CachePolicy): UserSummary {
        val steam64 = getSignedUserSteam64()
        return getUserSummary(steam64, cachePolicy)
    }

    private fun getSignedUserSteam64(): Long = userPreferences.getLong(SIGNED_USER_KEY)

    private suspend fun getUserSummary(
        steam64: Long,
        cachePolicy: CachePolicy
    ): UserSummary {
        val userSummaryEntity = userDataStoreFactory.create(steam64, cachePolicy)
            .getUserSummary(steam64)
        return mapper.mapFrom(userSummaryEntity)
    }

    override suspend fun signOut() {
        userPreferences.remove(SIGNED_USER_KEY)
    }

    companion object {
        const val SIGNED_USER_KEY = "signed_user_key"
    }
}