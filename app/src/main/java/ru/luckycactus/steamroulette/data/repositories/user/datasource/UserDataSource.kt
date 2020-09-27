package ru.luckycactus.steamroulette.data.repositories.user.datasource

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.SteamIdNotFoundException

interface UserDataSource {

    suspend fun getUserSummary(steamId: SteamId): UserSummaryEntity

    interface Local : UserDataSource {
        suspend fun saveUserSummary(userSummary: UserSummaryEntity)

        fun observeUserSummary(steamId: SteamId): Flow<UserSummaryEntity>

        suspend fun removeUserSummary(steamId: SteamId)
    }

    interface Remote : UserDataSource {

        @Throws(SteamIdNotFoundException::class)
        override suspend fun getUserSummary(steamId: SteamId): UserSummaryEntity
    }
}

