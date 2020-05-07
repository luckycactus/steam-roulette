package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary

interface UserRepository {

    suspend fun getUserSummary(steamId: SteamId, cachePolicy: CachePolicy): UserSummary?

    fun observeUserSummary(steamId: SteamId): Flow<UserSummary>

    fun setCurrentUser(steamId: SteamId)

    fun getCurrentUserSteamId(): SteamId?

    fun observeCurrentUserSteamId(): Flow<SteamId?>

    fun isUserSignedIn(): Boolean

    suspend fun signOut()

    suspend fun fetchUserSummary(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun clearUserSummary(steamId: SteamId)
}