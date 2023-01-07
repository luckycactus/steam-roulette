package ru.luckycactus.steamroulette.domain.app

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.domain.app.migrations.AppMigration
import javax.inject.Inject
import javax.inject.Provider

class MigrateAppUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val migrationsProvider: Provider<Map<Int, @JvmSuppressWildcards Provider<AppMigration>>>
) {
    suspend operator fun invoke() {
        val currentVersion = appRepository.currentVersion
        var lastVersion = appRepository.lastVersion

        if (lastVersion == 0) {
            appRepository.lastVersion = currentVersion
            return
        }

        if (lastVersion == currentVersion)
            return

        val migrations = migrationsProvider.get()
        while (lastVersion < currentVersion) {
            migrations[lastVersion]?.let {
                withContext(NonCancellable) {
                    it.get().migrate()
                    appRepository.lastVersion = lastVersion + 1
                }
            }
            lastVersion++
        }
        appRepository.lastVersion = currentVersion
    }
}