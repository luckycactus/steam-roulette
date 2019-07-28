package ru.luckycactus.steamroulette.data.user

import ru.luckycactus.steamroulette.domain.CachePolicy
import ru.luckycactus.steamroulette.domain.user.SteamId
import ru.luckycactus.steamroulette.domain.user.UserSummary

interface UserRepository {

    suspend fun getUserSummary(steamId: SteamId, cachePolicy: CachePolicy): UserSummary

    fun saveSignedInUser(steamId: SteamId)

    fun getSignedInUserSteamId(): SteamId

    suspend fun getSignedInUserSummary(cachePolicy: CachePolicy): UserSummary

    fun isUserSignedIn(): Boolean

    suspend fun signOut()
}