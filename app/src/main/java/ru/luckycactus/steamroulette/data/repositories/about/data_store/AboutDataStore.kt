package ru.luckycactus.steamroulette.data.repositories.about.data_store

import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary

interface AboutDataStore {
    suspend fun getAppLibraries(): List<AppLibrary>
}