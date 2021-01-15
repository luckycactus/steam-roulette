package ru.luckycactus.steamroulette.domain.app

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
import kotlin.time.Duration
import kotlin.time.days

@Singleton
class GamesPeriodicUpdater @Inject constructor(
    private val userSession: UserSession,
    private val gamesRepository: GamesRepository,
    private val scheduler: Scheduler,
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
                    enqueueWithDefaultIntervals(restart = false)
                else
                    scheduler.cancel()
            }
        }
        coroutineScope.launch {
            gamesUpdatesFlow.collect {
                enqueueWithDefaultIntervals(restart = true)
            }
        }
    }

    private fun enqueueWithDefaultIntervals(restart: Boolean) {
        scheduler.enqueue(REPEAT_INTERVAL, FLEX_INTERVAL, restart)
    }

    fun stop() {
        scheduler.cancel()
        coroutineScope.coroutineContext.cancelChildren()
    }

    class Work @Inject constructor(
        private val gamesRepository: GamesRepository,
        private val userSession: UserSession
    ) {
        suspend fun run(): Result =
            userSession.currentUser?.let {
                gamesRepository.updateOwnedGames(CachePolicy.Remote)
                Result.Success
            } ?: Result.Failure
    }

    enum class Result {
        Success, Failure
    }

    interface Scheduler {
        fun enqueue(repeatInterval: Duration, flexInterval: Duration, restart: Boolean)
        fun cancel()
    }

    private companion object {
        val REPEAT_INTERVAL = 6.days
        val FLEX_INTERVAL = 1.days
    }
}