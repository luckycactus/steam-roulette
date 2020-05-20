package ru.luckycactus.steamroulette.domain.app.migrations

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.core.invoke
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Provider

class Migration5to6 @Inject constructor(
    private val getCurrentUserSteamId: GetCurrentUserSteamIdUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    private val imageCacheCleaner: Provider<ImageCacheCleaner>
): AppMigration {

    override suspend fun migrate() {
        getCurrentUserSteamId()?.let {
            userSettingsRepository.migrateEnPlayTimeFilter(it)
        }
        imageCacheCleaner.get().clearAllCache()
    }
}