package ru.luckycactus.steamroulette.domain.app

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import ru.luckycactus.steamroulette.domain.core.invoke
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import javax.inject.Inject
import javax.inject.Provider

class MigrateAppUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val getCurrentUserSteamId: GetCurrentUserSteamIdUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    private val imageCacheCleaner: Provider<ImageCacheCleaner>
) : SuspendUseCase<Unit, Unit>() {

    private val migrations by lazyNonThreadSafe {
        mapOf(
            5 to ::migrate5to6
        )
    }

    override suspend fun getResult(params: Unit) {
        var lastVersion = appRepository.lastVersion
        val currentVersion = appRepository.currentVersion
        val isUserLoggedOn = getCurrentUserSteamId() != null
        if (lastVersion < 0) {
            if (isUserLoggedOn) lastVersion = 5 else return
        }
        while (lastVersion < currentVersion) {
            migrations[lastVersion]?.invoke()
            appRepository.lastVersion = ++lastVersion
        }
    }

    private suspend fun migrate5to6() {
        getCurrentUserSteamId()?.let {
            userSettingsRepository.migrateEnPlayTimeFilter(it)
        }
        imageCacheCleaner.get().clearAllCache()
    }
}