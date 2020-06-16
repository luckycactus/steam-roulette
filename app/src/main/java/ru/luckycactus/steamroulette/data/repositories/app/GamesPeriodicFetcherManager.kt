package ru.luckycactus.steamroulette.data.repositories.app

import android.content.Context
import androidx.work.*
import ru.luckycactus.steamroulette.di.common.BaseAppComponent
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.di.ForApplication
import ru.luckycactus.steamroulette.domain.app.GamesPeriodicFetcher
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesPeriodicFetcherManager @Inject constructor(
    @ForApplication private val context: Context
) : GamesPeriodicFetcher.Manager {

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

    class Worker(
        appContext: Context, params: WorkerParameters
    ) : CoroutineWorker(appContext, params) {

        @Inject
        lateinit var work: GamesPeriodicFetcher.Work

        init {
            InjectionManager.findComponent<BaseAppComponent>()
                .inject(this)
        }

        override suspend fun doWork(): Result {
            return work.run()
        }
    }

    companion object {
        private const val workName = "syncOwnedGamesWork"
    }
}