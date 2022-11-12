package ru.luckycactus.steamroulette.data.repositories.app

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.luckycactus.steamroulette.domain.app.GamesPeriodicUpdater
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class GamesPeriodicUpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : GamesPeriodicUpdater.Scheduler {

    override fun enqueue(repeatInterval: Duration, flexInterval: Duration, restart: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val work = PeriodicWorkRequestBuilder<Worker>(
            repeatInterval.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
            flexInterval.inWholeMilliseconds,
            TimeUnit.MILLISECONDS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                workName,
                if (restart) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
                work
            )
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    @HiltWorker
    class Worker @AssistedInject constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters
    ) : CoroutineWorker(appContext, params) {

        @Inject
        lateinit var work: GamesPeriodicUpdater.Work

        override suspend fun doWork(): Result {
            return when (work.run()) {
                GamesPeriodicUpdater.Result.Success -> Result.success()
                GamesPeriodicUpdater.Result.Failure -> Result.failure()
                GamesPeriodicUpdater.Result.Retry -> Result.retry()
            }
        }
    }

    companion object {
        private const val workName = "syncOwnedGamesWork"
    }
}