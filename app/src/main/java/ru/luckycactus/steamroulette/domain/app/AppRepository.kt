package ru.luckycactus.steamroulette.domain.app

import kotlinx.coroutines.flow.Flow

interface AppRepository {
    var lastVersion: Int
    val currentVersion: Int
    val currentVersionName: String

    fun observeSystemLocaleChanges(): Flow<Unit>
}