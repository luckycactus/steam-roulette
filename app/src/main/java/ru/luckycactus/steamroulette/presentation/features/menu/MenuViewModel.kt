package ru.luckycactus.steamroulette.presentation.features.menu

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.Clock
import ru.luckycactus.steamroulette.domain.core.RequestState
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesSyncsUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.user.ObserveUserSummarySyncsUseCase
import ru.luckycactus.steamroulette.domain.user.ObserveUserSummaryUseCase
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MenuViewModel @AssistedInject constructor(
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    observeOwnedGamesSyncsUseCase: ObserveOwnedGamesSyncsUseCase,
    observeUserSummary: ObserveUserSummaryUseCase,
    observeUserSummarySyncs: ObserveUserSummarySyncsUseCase,
    private val resourceManager: ResourceManager,
    @Assisted private val userViewModelDelegate: UserViewModelDelegate,
    private val router: Router,
    private val clock: Clock
) : BaseViewModel() {

    val userSummary = observeUserSummary().asLiveData()
    val gameCount: LiveData<Int> = observeOwnedGamesCount(GamesFilter.all()).asLiveData()
    val gamesLastUpdate: LiveData<String>
    val refreshProfileState: LiveData<Boolean>
    val closeAction: LiveData<Unit>
        get() = _closeAction
    val userSummaryLastSync
        get() = userSummarySyncs.value

    private val _closeAction = MutableLiveData<Unit>()
    private val userSummarySyncs =
        observeUserSummarySyncs().stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun refreshProfile() {
        userViewModelDelegate.fetchUserAndGames()
        closeWithDelay()
    }

    init {
        gamesLastUpdate = observeOwnedGamesSyncsUseCase()
            .map {
                val ago = if (it <= 0)
                    resourceManager.getString(R.string.last_sync_never)
                else
                    DateUtils.getRelativeTimeSpanString(
                        it,
                        clock.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                resourceManager.getString(R.string.games_last_sync, ago)
            }.asLiveData()

        refreshProfileState = combine(
            userViewModelDelegate.fetchUserSummaryState,
            userViewModelDelegate.fetchGamesState
        ) { userState, gamesState ->
            userState || (gamesState is RequestState.Loading)
        }.asLiveData()
    }

    fun onAboutClick() {
        router.navigateTo(Screens.About)
        close()
    }

    fun onLibraryClick() {
        router.navigateTo(Screens.Library)
        close()
    }

    fun logout() {
        userViewModelDelegate.logout()
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

    @AssistedFactory
    interface Factory {
        fun create(userViewModelDelegate: UserViewModelDelegate): MenuViewModel
    }

    companion object {
        private const val CLOSE_DELAY = 300L
    }
}