package ru.luckycactus.steamroulette.presentation.menu

import android.text.format.DateUtils
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesSyncsUseCase
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegatePublic
import ru.luckycactus.steamroulette.presentation.utils.combine

class MenuViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel(), UserViewModelDelegatePublic by userViewModelDelegate {

    val gameCount: LiveData<Int>
    val gamesLastUpdate: LiveData<String>
    val refreshProfileState: LiveData<Boolean>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    private val observeOwnedGamesCount = AppModule.observeOwnedGamesCountUseCase
    private val observeOwnedGamesSyncsUseCase = AppModule.observeOwnedGamesSyncsUseCase
    private val resourceManager = AppModule.resourceManager

    fun refreshProfile() {
        userViewModelDelegate.fetchUserAndGames()
        closeWithDelay()
    }

    init {
        gameCount = userViewModelDelegate.observeCurrentUserSteamId().switchMap {
            observeOwnedGamesCount(ObserveOwnedGamesCountUseCase.Params(it))
        }

        gamesLastUpdate =
            userViewModelDelegate.observeCurrentUserSteamId().switchMap {
                observeOwnedGamesSyncsUseCase(ObserveOwnedGamesSyncsUseCase.Params(it))
            }.map {
                val ago = if (it <= 0)
                    resourceManager.getString(R.string.never)
                else
                    DateUtils.getRelativeTimeSpanString(
                        it,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                resourceManager.getString(R.string.games_last_sync, ago)
            }

        refreshProfileState = userViewModelDelegate.fetchUserSummaryState.combine(
            userViewModelDelegate.fetchGamesState
        ) { a, b -> a || (b is Result.Loading) }
    }

    private fun closeWithDelay() {
        viewModelScope.launch {
            delay(CLOSE_DELAY)
            _closeAction.value = Unit
        }
    }

    companion object {
        private const val CLOSE_DELAY = 300L
    }
}