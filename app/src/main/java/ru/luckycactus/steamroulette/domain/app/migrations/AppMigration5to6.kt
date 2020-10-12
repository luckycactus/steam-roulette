package ru.luckycactus.steamroulette.domain.app.migrations

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import javax.inject.Inject
import javax.inject.Provider

class AppMigration5to6 @Inject constructor(
    private val imageCacheCleaner: Provider<ImageCacheCleaner>
) : AppMigration {

    override suspend fun migrate() {
        imageCacheCleaner.get().clearAllCache()
    }
}