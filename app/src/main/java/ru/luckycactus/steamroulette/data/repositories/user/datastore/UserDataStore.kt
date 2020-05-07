package ru.luckycactus.steamroulette.data.repositories.user.datastore

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId

interface UserDataStore {

    suspend fun getUserSummary(steamId: SteamId): UserSummaryEntity

    interface Local : UserDataStore {
        suspend fun saveUserSummary(userSummary: UserSummaryEntity)

        fun observeUserSummary(steamId: SteamId): Flow<UserSummaryEntity>

        fun setCurrentUser(steamId: SteamId)

        fun getCurrentUserSteam64(): SteamId?

        val currentUserSteamIdFlow: Flow<SteamId?>

        fun removeCurrentUserSteamId()

        suspend fun removeUserSummary(steamId: SteamId)
    }

    interface Remote : UserDataStore
}

