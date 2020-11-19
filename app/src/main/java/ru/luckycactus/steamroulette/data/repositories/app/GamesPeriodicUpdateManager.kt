package ru.luckycactus.steamroulette.data.repositories.app

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.luckycactus.steamroulette.domain.app.GamesPeriodicUpdater
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesPeriodicUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) : GamesPeriodicUpdater.Manager {

    override fun enqueue(restart: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val work = PeriodicWorkRequestBuilder<Worker>(5, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.DAYS)
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

    class Worker @WorkerInject constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters
    ) : CoroutineWorker(appContext, params) {

        @Inject
        lateinit var work: GamesPeriodicUpdater.Work

        override suspend fun doWork(): Result {
            return work.run()
        }
    }

    companion object {
        private const val workName = "syncOwnedGamesWork"
    }
}