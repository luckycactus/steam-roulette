package ru.luckycactus.steamroulette.data.user

import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

interface UserDataStore {

    suspend fun getUserSummary(steam64: Long): UserSummaryEntity
}