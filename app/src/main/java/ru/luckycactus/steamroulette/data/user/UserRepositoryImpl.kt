package ru.luckycactus.steamroulette.data.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import ru.luckycactus.steamroulette.domain.user.UserRepository
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(
    private val localUserDataStore: UserDataStore.Local,
    private val remoteUserDataStore: UserDataStore.Remote,
    private val mapper: UserSummaryMapper,
    private val userPreferences: PreferencesStorage, //todo replace by room
    private val networkBoundResourceFactory: NetworkBoundResource.Factory
) : UserRepository {
    //todo move to datastore?
    override fun saveSignedInUser(steamId: SteamId) {
        userPreferences[SIGNED_USER_KEY] = steamId.asSteam64()
    }

    override fun getSignedInUserSteamId(): SteamId? {
        val steam64 = userPreferences.getLong(SIGNED_USER_KEY)
        if (steam64 == 0L)
            return null
        return SteamId.fromSteam64(steam64)
    }

    override fun isUserSignedIn(): Boolean =
        userPreferences.getLong(SIGNED_USER_KEY) != 0L

    override suspend fun getUserSummary(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary {
        return createUserSummaryResource(steamId.asSteam64())
            .get(cachePolicy)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getUserSummaryCacheThenRemoteIfExpired(
        coroutineScope: CoroutineScope,
        steam64: Long
    ): ReceiveChannel<UserSummary> =
        createUserSummaryResource(steam64).getCacheThenRemoteIfExpired(coroutineScope)

    private fun createUserSummaryResource(
        steam64: Long
    ): NetworkBoundResource<UserSummaryEntity, UserSummary> {
        val cacheKey = "user_summary_$steam64"
        return networkBoundResourceFactory.create(
            cacheKey,
            cacheKey,
            SUMMARY_CACHE_WINDOW,
            getFromNetwork = { remoteUserDataStore.getUserSummary(steam64) },
            saveToCache = { localUserDataStore.saveUserSummaryToCache(it) },
            getFromCache = {
                val userSummaryEntity = localUserDataStore.getUserSummary(steam64)
                mapper.mapFrom(userSummaryEntity)
            }
        )
    }

    override suspend fun signOut() {
        userPreferences.remove(SIGNED_USER_KEY)
    }

    companion object {
        const val SIGNED_USER_KEY = "signed_user_key"
        val SUMMARY_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(4L, TimeUnit.SECONDS)
    }
}