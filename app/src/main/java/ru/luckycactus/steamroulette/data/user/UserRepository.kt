package ru.luckycactus.steamroulette.data.user

import ru.luckycactus.steamroulette.domain.user.SteamId
import ru.luckycactus.steamroulette.domain.user.UserSummary

interface UserRepository {

    suspend fun getUserSummary(steamId: SteamId, reload: Boolean): UserSummary

    fun saveSignedInUser(steamId: SteamId)

    fun getSignedInUserSteamId(): SteamId
}