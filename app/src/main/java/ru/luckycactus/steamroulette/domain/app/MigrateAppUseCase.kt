package ru.luckycactus.steamroulette.domain.app

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.domain.app.migrations.AppMigration
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject
import javax.inject.Provider

class MigrateAppUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val migrations: Map<Int, @JvmSuppressWildcards Provider<AppMigration>>
) : SuspendUseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit) {
        var lastVersion = appRepository.lastVersion
        val currentVersion = appRepository.currentVersion
        if (lastVersion == 0)
            lastVersion = currentVersion
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