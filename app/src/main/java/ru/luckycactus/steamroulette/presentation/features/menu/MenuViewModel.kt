package ru.luckycactus.steamroulette.presentation.features.menu

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.Clock
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.RequestState
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesSyncsUseCase
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegatePublic
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.combine
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MenuViewModel @Inject constructor(
    private val observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    private val observeOwnedGamesSyncsUseCase: ObserveOwnedGamesSyncsUseCase,
    private val resourceManager: ResourceManager,
    private val userViewModelDelegate: UserViewModelDelegate,
    private val router: Router,
    private val clock: Clock
) : BaseViewModel(), UserViewModelDelegatePublic by userViewModelDelegate {

    val gameCount: LiveData<Int> = userViewModelDelegate.currentUserSteamId
        .flatMapLatest { observeOwnedGamesCount(ObserveOwnedGamesCountUseCase.Params(it)) }
        .asLiveData()
    val gamesLastUpdate: LiveData<String>
    val refreshProfileState: LiveData<Boolean>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    fun refreshProfile() {
        userViewModelDelegate.fetchUserAndGames()
        closeWithDelay()
    }

    init {
        gamesLastUpdate =
            userViewModelDelegate.currentUserSteamId.flatMapLatest {
                observeOwnedGamesSyncsUseCase(ObserveOwnedGamesSyncsUseCase.Params(it))
            }.map {
                val ago = if (it <= 0)
                    resourceManager.getString(R.string.never)
                else
                    DateUtils.getRelativeTimeSpanString(
                        it,
                        clock.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                resourceManager.getString(R.string.games_last_sync, ago)
            }.asLiveData()

        refreshProfileState = userViewModelDelegate.fetchUserSummaryState.combine(
            userViewModelDelegate.fetchGamesState
        ) { a, b -> a || (b is RequestState.Loading) }
    }

    fun onAboutClick() {
        router.navigateTo(Screens.About)
        close()
    }

    fun exit() {
        userViewModelDelegate.exit()
    }

    private fun closeWithDelay() {
        viewModelScope.launch {
            delay(CLOSE_DELAY)
            close()
        }
    }

    private fun close() {
        _closeAction.value = Unit
    }

    companion object {
        private const val CLOSE_DELAY = 300L
    }
}