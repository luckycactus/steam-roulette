package ru.luckycactus.steamroulette.domain.app

import androidx.work.ListenableWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.common.switchNullsToEmpty
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesPeriodicFetcher @Inject constructor(
    private val userRepository: UserRepository,
    private val gamesRepository: GamesRepository,
    private val manager: Manager,
    @ForApplication private val appScope: CoroutineScope
) {
    private val coroutineScope = appScope + Job(appScope.coroutineContext[Job])

    private val currentUserFlow = userRepository.observeCurrentUserSteamId()
    private val gamesUpdatesFlow =
        currentUserFlow.switchNullsToEmpty()
            .flatMapLatest { gamesRepository.observeGamesUpdates(it).drop(1) }

    fun start() {
        coroutineScope.launch {
            currentUserFlow.collect {
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
        private val userRepository: UserRepository
    ) {
        suspend fun run(): ListenableWorker.Result =
            userRepository.getCurrentUserSteamId()?.let {
                gamesRepository.fetchOwnedGames(it, CachePolicy.Remote)
                ListenableWorker.Result.success()
            } ?: ListenableWorker.Result.failure()
    }

    interface Manager {
        fun enqueue(restart: Boolean)
        fun cancel()
    }
}