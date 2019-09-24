package ru.luckycactus.steamroulette.data.user.datastore

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

interface UserDataStore {

    suspend fun getUserSummary(steam64: Long): UserSummaryEntity

    interface Local : UserDataStore {
        suspend fun saveUserSummaryToCache(userSummary: UserSummaryEntity)
    }

    interface Remote : UserDataStore
}

