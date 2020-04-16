package ru.luckycactus.steamroulette.data.repositories.about

import dagger.Reusable
import ru.luckycactus.steamroulette.data.repositories.about.data_store.AboutDataStore
import ru.luckycactus.steamroulette.domain.about.AboutRepository
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import javax.inject.Inject

@Reusable
class AboutRepositoryImpl @Inject constructor(
    private val aboutDataStore: AboutDataStore
) : AboutRepository {

    override suspend fun getAppLibraries(): List<AppLibrary> {
        return aboutDataStore.getAppLibraries()
    }
}