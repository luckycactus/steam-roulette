package ru.luckycactus.steamroulette.domain.user

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

interface UserRepository {

    suspend fun getUserSummary(steamId: SteamId, cachePolicy: CachePolicy): UserSummary

    fun observeCurrentUserSummary(): LiveData<UserSummary?>

    fun saveSignedInUser(steamId: SteamId)

    fun getCurrentUserSteamId(): SteamId?

    fun observeCurrentUserSteamId(): LiveData<SteamId?>

    fun isUserSignedIn(): Boolean

    suspend fun signOut()

    suspend fun getUserSummaryCacheThenRemoteIfExpired(
        coroutineScope: CoroutineScope,
        steamId: SteamId
    ): ReceiveChannel<UserSummary>

    suspend fun refreshUserSummary(steamId: SteamId, cachePolicy: CachePolicy)
}