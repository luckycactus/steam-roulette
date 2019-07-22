package ru.luckycactus.steamroulette.data.user

import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

class LocalUserDataStore(
    private val userCache: UserCache
) : UserDataStore {

    override suspend fun getUserSummary(steam64: Long): UserSummaryEntity =
        userCache.getUserSummary(steam64)
}