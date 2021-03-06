package ru.luckycactus.steamroulette.domain.app.migrations

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import javax.inject.Inject

class AppMigration5to6 @Inject constructor(
    private val imageCacheCleaner: ImageCacheCleaner
) : AppMigration {

    override suspend fun migrate() {
        imageCacheCleaner.clearDiskCache()
    }
}