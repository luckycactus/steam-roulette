package ru.luckycactus.steamroulette.data.repositories.about.datasource

import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary

interface AboutDataSource {
    suspend fun getAppLibraries(): List<AppLibrary>
}