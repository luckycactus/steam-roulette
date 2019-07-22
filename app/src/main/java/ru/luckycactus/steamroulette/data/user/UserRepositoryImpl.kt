package ru.luckycactus.steamroulette.data.user

import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.local.get
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.CachePolicy
import ru.luckycactus.steamroulette.domain.user.SteamId
import ru.luckycactus.steamroulette.domain.user.UserSummary

class UserRepositoryImpl(
    private val userDataStoreFactory: UserDataStoreFactory,
    private val mapper: UserSummaryMapper,
    private val userPreferences: PreferencesStorage //todo replace by room
) : UserRepository {

    companion object {
        const val SIGNED_USER_KEY = "signed_user_key"
    }

    override suspend fun getUserSummary(
        steamId: SteamId,
        reload: Boolean
    ): UserSummary {
        val cachePolicy = if (reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        val userSummaryEntity = userDataStoreFactory.create(steamId.asSteam64(), cachePolicy)
            .getUserSummary(steamId.asSteam64())
        return mapper.mapFrom(userSummaryEntity)
    }

    //todo move to datastore?
    override fun saveSignedInUser(steamId: SteamId) {
        userPreferences[SIGNED_USER_KEY] = steamId.asSteam64()
    }

    override fun getSignedInUserSteamId(): SteamId =
        userPreferences[SIGNED_USER_KEY]
}