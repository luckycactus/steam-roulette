package ru.luckycactus.steamroulette.data.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

interface UserRepository {

    suspend fun getUserSummary(steamId: SteamId, cachePolicy: CachePolicy): UserSummary

    fun saveSignedInUser(steamId: SteamId)

    fun getSignedInUserSteamId(): SteamId?

    fun isUserSignedIn(): Boolean

    suspend fun signOut()

    suspend fun getUserSummaryCacheThenRemoteIfExpired(
        coroutineScope: CoroutineScope,
        steam64: Long
    ): ReceiveChannel<UserSummary>
}