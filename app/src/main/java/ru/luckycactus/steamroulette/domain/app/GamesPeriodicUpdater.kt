package ru.luckycactus.steamroulette.domain.app

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.UpdateOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

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
        private val updateOwnedGames: UpdateOwnedGamesUseCase,
        private val userSession: UserSession
    ) {
        suspend fun run(): Result {
            if (userSession.isUserLoggedIn) {
                return when (updateOwnedGames(UpdateOwnedGamesUseCase.Params(reload = true))) {
                    UpdateOwnedGamesUseCase.Result.Success -> Result.Success
                    UpdateOwnedGamesUseCase.Result.Fail.PrivateProfile -> Result.Failure
                    is UpdateOwnedGamesUseCase.Result.Fail.Error -> Result.Retry
                }
            }
            return Result.Success
        }

    }

    enum class Result {
        Success, Failure, Retry
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