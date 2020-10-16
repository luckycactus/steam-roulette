package ru.luckycactus.steamroulette.data.repositories.user

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.core.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.user.datasource.UserDataSource
import ru.luckycactus.steamroulette.data.repositories.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject
import kotlin.time.days

@Reusable
class UserRepositoryImpl @Inject constructor(
    private val userSession: UserSession,
    private val localUserDataSource: UserDataSource.Local,
    private val remoteUserDataSource: UserDataSource.Remote,
    private val mapper: UserSummaryMapper
) : UserRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    override suspend fun getUserSummaryOrThrow(cachePolicy: CachePolicy): UserSummary =
        getUserSummaryOrThrow(currentUser, cachePolicy)

    override suspend fun getUserSummaryOrThrow(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary = createUserSummaryNBR(steamId).getOrThrow(cachePolicy)

    override fun observeUserSummary(): Flow<UserSummary> =
        localUserDataSource.observeSummary(currentUser)
            .map { mapper.mapFrom(it) }

    override suspend fun fetchUserSummary(cachePolicy: CachePolicy) {
        createUserSummaryNBR(currentUser).fetchIfNeed(cachePolicy)
    }

    override suspend fun clearUser(steamId: SteamId) {
        localUserDataSource.removeSummary(steamId)
        createUserSummaryNBR(steamId).invalidateCache()
    }

    override fun observeSummaryUpdates(): Flow<Long> =
        createUserSummaryNBR(currentUser).observeCacheUpdates()

    private fun createUserSummaryNBR(
        steamId: SteamId
    ): NetworkBoundResource.FullCache<UserSummaryEntity, UserSummary> {
        val cacheKey = "user_summary_${steamId.as64()}"
        return object : NetworkBoundResource.FullCache<UserSummaryEntity, UserSummary>(
            cacheKey,
            cacheKey,
            SUMMARY_CACHE_WINDOW
        ) {
            private var result: UserSummary? = null

            override suspend fun fetch(): UserSummaryEntity {
                return remoteUserDataSource.getSummary(steamId)
            }

            override suspend fun saveToStorage(data: UserSummaryEntity) {
                localUserDataSource.saveSummary(data)
                result = mapper.mapFrom(data)
            }

            override suspend fun getFromStorage(): UserSummary {
                return result ?: mapper.mapFrom(localUserDataSource.getSummary(steamId))
            }
        }
    }

    companion object {
        val SUMMARY_CACHE_WINDOW = 7.days
    }
}