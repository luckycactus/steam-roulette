package ru.luckycactus.steamroulette.domain.app.migrations

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Provider

class AppMigration5to6 @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    private val imageCacheCleaner: Provider<ImageCacheCleaner>
): AppMigration {

    override suspend fun migrate() {
        getCurrentUser()?.let {
            userSettingsRepository.migrateEnPlayTimeFilter(it)
        }
        imageCacheCleaner.get().clearAllCache()
    }
}