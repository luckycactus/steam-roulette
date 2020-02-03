package ru.luckycactus.steamroulette.data.repositories.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.repositories.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.common.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import ru.luckycactus.steamroulette.domain.user.UserRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localUserDataStore: UserDataStore.Local,
    private val remoteUserDataStore: UserDataStore.Remote,
    private val mapper: UserSummaryMapper
) : UserRepository {

    override fun setCurrentUser(steamId: SteamId) {
        localUserDataStore.setCurrentUser(steamId.asSteam64())
    }

    override fun getCurrentUserSteamId() = fromSteam64(localUserDataStore.getCurrentUserSteam64())

    override fun observeCurrentUserSteamId(): LiveData<SteamId?> =
        localUserDataStore.observeCurrentUserSteam64().map {
            fromSteam64(it)
        }

    override fun isUserSignedIn(): Boolean =
        localUserDataStore.getCurrentUserSteam64() != 0L

    override suspend fun getUserSummary(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary? = createUserSummaryResource(steamId).get(cachePolicy)

    override fun observeUserSummary(steamId: SteamId): LiveData<UserSummary> =
        localUserDataStore.observeUserSummary(steamId.asSteam64())
            .map { mapper.mapFrom(it) }

    override suspend fun fetchUserSummary(steamId: SteamId, cachePolicy: CachePolicy) {
        createUserSummaryResource(steamId).updateIfNeed(cachePolicy)
    }

    override suspend fun signOut() {
        localUserDataStore.removeCurrentUserSteam64()
    }

    override suspend fun clearUserSummary(steamId: SteamId) {
        localUserDataStore.removeUserSummary(steamId.asSteam64())
        createUserSummaryResource(steamId).invalidateCache()
    }

    private fun fromSteam64(steam64: Long) =
        if (steam64 == 0L) null else SteamId.fromSteam64(steam64)

    private fun createUserSummaryResource(
        steamId: SteamId
    ): NetworkBoundResource<UserSummaryEntity, UserSummary> {
        val steam64 = steamId.asSteam64()
        val cacheKey = "user_summary_$steam64"
        return object : NetworkBoundResource<UserSummaryEntity, UserSummary>(
            cacheKey,
            cacheKey,
            SUMMARY_CACHE_WINDOW
        ) {
            private var result: UserSummary? = null

            override suspend fun getFromNetwork(): UserSummaryEntity {
                return remoteUserDataStore.getUserSummary(steam64)
            }

            override suspend fun saveToCache(data: UserSummaryEntity) {
                localUserDataStore.saveUserSummary(data)
                result = mapper.mapFrom(data)
            }

            override suspend fun getFromCache(): UserSummary {
                return result ?: mapper.mapFrom(localUserDataStore.getUserSummary(steam64))
            }
        }
    }

    companion object {
        val SUMMARY_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.DAYS)
    }
}