package ru.luckycactus.steamroulette.data

import android.content.Context
import androidx.work.*
import dagger.Reusable
import ru.luckycactus.steamroulette.di.common.BaseAppComponent
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.app.SyncGamesPeriodicJob
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.FetchUserOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserSteamIdUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class SyncGamesPeriodicJobWorkManagerImpl @Inject constructor(
    @ForApplication private val context: Context
) : SyncGamesPeriodicJob {

    override fun start(restart: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val work = PeriodicWorkRequestBuilder<SyncGamesPeriodicWorker>(
            5,
            TimeUnit.DAYS,
            1,
            TimeUnit.DAYS
        ).setConstraints(constraints)
            .setInitialDelay(4, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                workName,
                if (restart) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
                work
            )
    }

    override fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    class SyncGamesPeriodicWorker(
        appContext: Context, params: WorkerParameters
    ) : CoroutineWorker(appContext, params) {

        @Inject
        lateinit var fetchUserOwnedGames: FetchUserOwnedGamesUseCase

        @Inject
        lateinit var getCurrentUserSteamId: GetCurrentUserSteamIdUseCase

        init {
            InjectionManager.findComponent<BaseAppComponent>()
                .inject(this)
        }

        override suspend fun doWork(): Result {
            return getCurrentUserSteamId()?.let {
                fetchUserOwnedGames(FetchUserOwnedGamesUseCase.Params(it, true))
                Result.success()
            } ?: Result.failure()
        }
    }

    companion object {
        private const val workName = "syncOwnedGamesWork"
    }
}