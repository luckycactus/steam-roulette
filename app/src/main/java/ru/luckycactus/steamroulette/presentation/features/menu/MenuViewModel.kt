package ru.luckycactus.steamroulette.presentation.features.menu

import android.text.format.DateUtils
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.Clock
import ru.luckycactus.steamroulette.domain.core.RequestState
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesSyncsUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.user.ObserveUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel

class MenuViewModel @AssistedInject constructor(
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    observeOwnedGamesSyncsUseCase: ObserveOwnedGamesSyncsUseCase,
    observeUserSummary: ObserveUserSummaryUseCase,
    private val resourceManager: ResourceManager,
    @Assisted private val userViewModelDelegate: UserViewModelDelegate,
    private val router: Router,
    private val clock: Clock
) : BaseViewModel() {

    val state: StateFlow<UiState?>

    private val _closeAction = Channel<Unit>(capacity = Channel.BUFFERED)
    val closeAction = _closeAction.receiveAsFlow()

    init {
        val gamesLastUpdate = observeOwnedGamesSyncsUseCase()
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
            }

        val refreshProfileState = combine(
            userViewModelDelegate.fetchUserSummaryState,
            userViewModelDelegate.fetchGamesState
        ) { userState, gamesState ->
            userState || (gamesState is RequestState.Loading)
        }

        state = combine(
            observeUserSummary(),
            observeOwnedGamesCount(GamesFilter.all()),
            gamesLastUpdate,
            refreshProfileState,
            ::UiState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    }

    fun refreshProfile() {
        userViewModelDelegate.fetchUserAndGames()
        closeWithDelay()
    }

    fun onAboutClick() {
        router.navigateTo(Screens.About())
        close()
    }

    fun onOldLibraryClick() {
        router.navigateTo(Screens.OldLibrary())
        close()
    }

    fun onLibraryClick() {
        router.navigateTo(Screens.Library())
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
        _closeAction.trySend(Unit)
    }

    @AssistedFactory
    interface Factory {
        fun create(userViewModelDelegate: UserViewModelDelegate): MenuViewModel
    }

    data class UiState(
        val userSummary: UserSummary,
        val gamesCount: Int,
        val gamesLastUpdate: String,
        val refreshState: Boolean,
    )

    companion object {
        private const val CLOSE_DELAY = 300L
    }
}