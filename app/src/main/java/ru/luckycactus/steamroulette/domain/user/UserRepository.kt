package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary

interface UserRepository {

    @Throws(SteamIdNotFoundException::class)
    suspend fun getUserSummaryOrThrow(steamId: SteamId, cachePolicy: CachePolicy): UserSummary

    @Throws(SteamIdNotFoundException::class)
    suspend fun getUserSummaryOrThrow(cachePolicy: CachePolicy): UserSummary

    fun observeUserSummary(): Flow<UserSummary>

    suspend fun fetchUserSummary(cachePolicy: CachePolicy)

    suspend fun clearUser(steamId: SteamId)

    fun observeSummaryUpdates(): Flow<Long>
}

class SteamIdNotFoundException(val steamId: String) : Exception()