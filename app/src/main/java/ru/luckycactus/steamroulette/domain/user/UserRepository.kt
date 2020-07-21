package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary

interface UserRepository {

    @Throws(SteamIdNotFoundException::class)
    suspend fun getUserSummary(steamId: SteamId, cachePolicy: CachePolicy): UserSummary?

    @Throws(SteamIdNotFoundException::class)
    suspend fun getUserSummary(cachePolicy: CachePolicy): UserSummary?

    fun observeUserSummary(): Flow<UserSummary>

    suspend fun fetchUserSummary(cachePolicy: CachePolicy)

    suspend fun clearUser(steamId: SteamId)
}

class SteamIdNotFoundException(val steamId: String) : Exception()