package ru.luckycactus.steamroulette.data.user.datastore

import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

interface UserDataStore {

    suspend fun getUserSummary(steam64: Long): UserSummaryEntity

    interface Local : UserDataStore {
        fun saveUserSummaryToCache(userSummary: UserSummaryEntity)
    }

    interface Remote : UserDataStore
}

