package ru.luckycactus.steamroulette.data.repositories.user.datastore

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId

interface UserDataStore {

    suspend fun getUserSummary(steamId: SteamId): UserSummaryEntity

    interface Local : UserDataStore {
        suspend fun saveUserSummary(userSummary: UserSummaryEntity)

        fun observeUserSummary(steamId: SteamId): LiveData<UserSummaryEntity>

        fun setCurrentUser(steamId: SteamId)

        fun getCurrentUserSteam64(): SteamId?

        fun observeCurrentUserSteam64(): LiveData<SteamId?>

        fun removeCurrentUserSteam64()

        suspend fun removeUserSummary(steamId: SteamId)
    }

    interface Remote : UserDataStore
}

