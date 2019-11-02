package ru.luckycactus.steamroulette.domain.user

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

interface UserRepository {

    suspend fun getUserSummary(steamId: SteamId, cachePolicy: CachePolicy): UserSummary

    fun observeUserSummary(steamId: SteamId): LiveData<UserSummary>

    fun setCurrentUser(steamId: SteamId)

    fun getCurrentUserSteamId(): SteamId?

    fun observeCurrentUserSteamId(): LiveData<SteamId?>

    fun isUserSignedIn(): Boolean

    suspend fun signOut()

    suspend fun fetchUserSummary(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun clearUserSummary(steamId: SteamId)
}