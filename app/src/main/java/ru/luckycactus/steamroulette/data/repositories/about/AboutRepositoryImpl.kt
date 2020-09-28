package ru.luckycactus.steamroulette.data.repositories.about

import ru.luckycactus.steamroulette.data.repositories.about.datasource.AboutDataSource
import ru.luckycactus.steamroulette.domain.about.AboutRepository
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import javax.inject.Inject

class AboutRepositoryImpl @Inject constructor(
    private val aboutDataSource: AboutDataSource
) : AboutRepository {

    override suspend fun getAppLibraries(): List<AppLibrary> = aboutDataSource.getAppLibraries()
}