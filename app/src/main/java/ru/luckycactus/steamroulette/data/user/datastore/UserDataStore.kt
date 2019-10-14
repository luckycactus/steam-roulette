package ru.luckycactus.steamroulette.data.user.datastore

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.entity.SteamId

interface UserDataStore {

    suspend fun getUserSummary(steam64: Long): UserSummaryEntity

    interface Local : UserDataStore {
        suspend fun saveUserSummaryToCache(userSummary: UserSummaryEntity)

        fun observeUserSummary(steam64: Long): LiveData<UserSummaryEntity>

        fun saveSignedInUser(steam64: Long)

        fun getCurrentUserSteam64(): Long

        fun observeCurrentUserSteam64(): LiveData<Long>

        fun removeCurrentUserSteam64()
    }

    interface Remote : UserDataStore
}

