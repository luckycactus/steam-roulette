package ru.luckycactus.steamroulette.domain.update

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import javax.inject.Inject

class MigrateAppUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val getCurrentUserSteamId: GetCurrentUserSteamIdUseCase,
    private val userSettingsRepository: UserSettingsRepository
) : SuspendUseCase<Unit, Unit>() {

    private val migrations by lazyNonThreadSafe {
        mapOf(
            4 to ::migrate4to5
        )
    }

    override suspend fun getResult(params: Unit) {
        var lastVersion = appSettingsRepository.lastVersion
        val currentVersion = appSettingsRepository.currentVersion
        val isUserLoggedOn = getCurrentUserSteamId() != null
        if (lastVersion < 0) {
            if (isUserLoggedOn) lastVersion = 4 else return
        }
        while (lastVersion < currentVersion) {
            migrations[lastVersion]?.invoke()
            appSettingsRepository.lastVersion = ++lastVersion
        }
    }

    fun migrate4to5() {
        getCurrentUserSteamId()?.let {
            userSettingsRepository.migrateEnPlayTimeFilter(it)
        }
    }
}