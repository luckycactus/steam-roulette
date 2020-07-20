package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary

interface UserRepository {

    @Throws(SteamIdNotFoundException::class)
    suspend fun getUserSummary(steamId: SteamId, cachePolicy: CachePolicy): UserSummary?

    fun observeUserSummary(steamId: SteamId): Flow<UserSummary>

    suspend fun fetchUserSummary(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun clearUser(steamId: SteamId)
}

class SteamIdNotFoundException(val steamId: String): Exception()