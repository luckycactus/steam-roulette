package ru.luckycactus.steamroulette.domain.about

import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary

interface AboutRepository {
    suspend fun getAppLibraries(): List<AppLibrary>
}