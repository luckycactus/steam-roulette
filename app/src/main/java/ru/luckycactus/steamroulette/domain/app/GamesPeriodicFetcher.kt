package ru.luckycactus.steamroulette.domain.app

import androidx.work.ListenableWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesPeriodicFetcher @Inject constructor(
    private val userSession: UserSession,
    private val gamesRepository: GamesRepository,
    private val manager: Manager,
    @AppCoScope private val appScope: CoroutineScope
) {
    private val coroutineScope = appScope + Job(appScope.coroutineContext[Job])

    private val gamesUpdatesFlow =
        userSession.observeCurrentUser().flatMapLatest {
            it?.let { gamesRepository.observeGamesUpdates().drop(1) } ?: emptyFlow()
        }

    fun start() {
        coroutineScope.launch {
            userSession.observeCurrentUser().collect {
                if (it != null)
                    manager.enqueue(false)
                else
                    manager.cancel()
            }
        }
        coroutineScope.launch {
            gamesUpdatesFlow.collect {
                manager.enqueue(true)
            }
        }
    }

    fun stop() {
        manager.cancel()
        coroutineScope.coroutineContext.cancelChildren()
    }

    class Work @Inject constructor(
        private val gamesRepository: GamesRepository,
        private val userSession: UserSession
    ) {
        suspend fun run(): ListenableWorker.Result =
            userSession.currentUser?.let {
                gamesRepository.fetchOwnedGames(CachePolicy.Remote)
                ListenableWorker.Result.success()
            } ?: ListenableWorker.Result.failure()
    }

    interface Manager {
        fun enqueue(restart: Boolean)
        fun cancel()
    }
}