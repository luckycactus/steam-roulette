package ru.luckycactus.steamroulette.data.repositories.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.data.core.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.repositories.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import ru.luckycactus.steamroulette.domain.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.days

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localUserDataStore: UserDataStore.Local,
    private val remoteUserDataStore: UserDataStore.Remote,
    private val mapper: UserSummaryMapper
) : UserRepository {

    override fun setCurrentUser(steamId: SteamId) {
        localUserDataStore.setCurrentUser(steamId)
    }

    override fun getCurrentUserSteamId(): SteamId? = localUserDataStore.getCurrentUserSteam64()

    override fun observeCurrentUserSteamId(): LiveData<SteamId?> =
        localUserDataStore.observeCurrentUserSteam64()

    override fun isUserSignedIn(): Boolean = localUserDataStore.getCurrentUserSteam64() != null

    override suspend fun getUserSummary(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary? = createUserSummaryResource(steamId).get(cachePolicy)

    override fun observeUserSummary(steamId: SteamId): LiveData<UserSummary> =
        localUserDataStore.observeUserSummary(steamId)
            .map { mapper.mapFrom(it) }

    override suspend fun fetchUserSummary(steamId: SteamId, cachePolicy: CachePolicy) {
        createUserSummaryResource(steamId).updateIfNeed(cachePolicy)
    }

    override suspend fun signOut() {
        localUserDataStore.removeCurrentUserSteam64()
    }

    override suspend fun clearUserSummary(steamId: SteamId) {
        localUserDataStore.removeUserSummary(steamId)
        createUserSummaryResource(steamId).invalidateCache()
    }

    private fun createUserSummaryResource(
        steamId: SteamId
    ): NetworkBoundResource<UserSummaryEntity, UserSummary> {
        val cacheKey = "user_summary_${steamId.asSteam64()}"
        return object : NetworkBoundResource<UserSummaryEntity, UserSummary>(
            cacheKey,
            cacheKey,
            SUMMARY_CACHE_WINDOW
        ) {
            private var result: UserSummary? = null

            override suspend fun getFromNetwork(): UserSummaryEntity {
                return remoteUserDataStore.getUserSummary(steamId)
            }

            override suspend fun saveToCache(data: UserSummaryEntity) {
                localUserDataStore.saveUserSummary(data)
                result = mapper.mapFrom(data)
            }

            override suspend fun getFromCache(): UserSummary {
                return result ?: mapper.mapFrom(localUserDataStore.getUserSummary(steamId))
            }
        }
    }

    companion object {
        val SUMMARY_CACHE_WINDOW = 7.days
    }
}