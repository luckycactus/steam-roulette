package ru.luckycactus.steamroulette.data.repositories.user

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.core.NetworkBoundResource
import ru.luckycactus.steamroulette.data.repositories.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.repositories.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject
import kotlin.time.days

@Reusable
class UserRepositoryImpl @Inject constructor(
    private val localUserDataStore: UserDataStore.Local,
    private val remoteUserDataStore: UserDataStore.Remote,
    private val mapper: UserSummaryMapper
) : UserRepository {

    override suspend fun getUserSummary(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary? = createUserSummaryResource(steamId).get(cachePolicy)

    override fun observeUserSummary(steamId: SteamId): Flow<UserSummary> =
        localUserDataStore.observeUserSummary(steamId)
            .map { mapper.mapFrom(it) }

    override suspend fun fetchUserSummary(steamId: SteamId, cachePolicy: CachePolicy) {
        createUserSummaryResource(steamId).updateIfNeed(cachePolicy)
    }

    override suspend fun clearUser(steamId: SteamId) {
        localUserDataStore.removeUserSummary(steamId)
        createUserSummaryResource(steamId).invalidateCache()
    }

    private fun createUserSummaryResource(
        steamId: SteamId
    ): NetworkBoundResource<UserSummaryEntity, UserSummary> {
        val cacheKey = "user_summary_${steamId.as64()}"
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