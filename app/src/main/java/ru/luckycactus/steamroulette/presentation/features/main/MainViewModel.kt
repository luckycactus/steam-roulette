package ru.luckycactus.steamroulette.presentation.features.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.app.MigrateAppUseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.common.toStateFlow
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.RequestState
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.Result
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.ClearHiddenGamesUseCase
import ru.luckycactus.steamroulette.domain.games.FetchUserOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.login.LogoutUserUseCase
import ru.luckycactus.steamroulette.domain.user.FetchUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.ObserveCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user.ObserveUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MainViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserSteamIdUseCase,
    private val observeUserSummary: ObserveUserSummaryUseCase,
    private val fetchUserSummary: FetchUserSummaryUseCase,
    private val fetchUserOwnedGames: FetchUserOwnedGamesUseCase,
    private val logoutUser: LogoutUserUseCase,
    private val migrateApp: MigrateAppUseCase,
    private val clearHiddenGames: ClearHiddenGamesUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel(), UserViewModelDelegate {
    override val isUserLoggedIn: Boolean
        get() = _nullableCurrentUserSteamIdFlow.value != null

    override val userSummary: LiveData<UserSummary>
    override val fetchGamesState: LiveData<RequestState<Unit>>
        get() = _fetchGamesState
    override val fetchUserSummaryState: LiveData<Boolean>
        get() = _fetchUserSummaryState
    override val currentUserSteamId: Flow<SteamId>

    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    private val _nullableCurrentUserSteamIdFlow: StateFlow<SteamId?>

    private val _fetchGamesState = MutableLiveData<RequestState<Unit>>()
    private val _fetchUserSummaryState = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<Event<String>>()

    private val userScope = viewModelScope + SupervisorJob(viewModelScope.coroutineContext[Job])

    init {
        _nullableCurrentUserSteamIdFlow = observeCurrentUser().toStateFlow(viewModelScope, null)

        currentUserSteamId = _nullableCurrentUserSteamIdFlow.filterNotNull()

        userSummary = currentUserSteamId
            .flatMapLatest { observeUserSummary(it) }
            .asLiveData()

        viewModelScope.launch {
            _nullableCurrentUserSteamIdFlow.collect {
                userScope.coroutineContext.cancelChildren()

                it?.let {
                    userScope.launch {
                        fetchGames(false)
                    }
                    userScope.launch {
                        fetchUserSummary(false)
                    }
                }
            }
        }
    }

    fun onColdStart() {
        viewModelScope.launch {
            migrateApp()
            _nullableCurrentUserSteamIdFlow.first().let {
                val screen = if (it != null) Screens.Roulette else Screens.Login
                router.newRootScreen(screen)
            }
        }
    }

    fun onGameClick(game: GameHeader, enableSharedElementTransition: Boolean) {
        router.navigateTo(Screens.GameDetails(game, enableSharedElementTransition))
    }

    override fun resetHiddenGames() {
        userScope.launch {
            clearHiddenGames(getCurrentUserSteamId())
        }
    }

    override fun exit() {
        userScope.coroutineContext.cancelChildren()
        router.newRootScreen(Screens.Login)
        userScope.launch {
            logoutUser()
        }
    }

    override fun getCurrentUserSteamId(): SteamId {
        return _nullableCurrentUserSteamIdFlow.value!!
    }

    override fun fetchGames() {
        userScope.launch {
            handleGamesFetchError(fetchGames(true))
        }
    }

    override fun fetchUserAndGames() {
        userScope.launch {
            supervisorScope {
                val userDeferred = userScope.async { fetchUserSummary(true) }
                val gamesState = fetchGames(true).also { handleGamesFetchError(it) }
                if (gamesState !is RequestState.Error) {
                    handleUserFetchError(userDeferred.await())
                }
            }
        }
    }

    private fun handleUserFetchError(requestState: RequestState<Unit>) {
        if (requestState is RequestState.Error) {
            _errorMessage.value = Event(
                "%s: %s".format(
                    resourceManager.getString(R.string.error_user_update),
                    requestState.message
                )
            )
        }
    }


    private fun handleGamesFetchError(requestState: RequestState<Unit>) {
        if (requestState is RequestState.Error) {
            _errorMessage.value = Event(
                "%s: %s".format(
                    resourceManager.getString(R.string.error_get_owned_games),
                    requestState.message
                )
            )
        }
    }

    private suspend fun fetchGames(reload: Boolean): RequestState<Unit> {
        getCurrentUserSteamId().let {
            _fetchGamesState.value = RequestState.Loading
            val result = fetchUserOwnedGames(FetchUserOwnedGamesUseCase.Params(it, reload))
            return when (result) {
                is FetchUserOwnedGamesUseCase.Result.Success -> {
                    RequestState.success.also { _fetchGamesState.value = it }
                }
                is FetchUserOwnedGamesUseCase.Result.Fail -> {
                    val message = when (result) {
                        FetchUserOwnedGamesUseCase.Result.Fail.PrivateProfile ->
                            resourceManager.getString(R.string.error_get_owned_games_privacy)
                        is FetchUserOwnedGamesUseCase.Result.Fail.Error -> {
                            result.cause.printStackTrace()
                            resourceManager.getCommonErrorDescription(result.cause)
                        }
                    }
                    RequestState.Error(message).also {
                        _fetchGamesState.value = it
                    }
                }
            }
        }
    }

    private suspend fun fetchUserSummary(reload: Boolean): RequestState<Unit> {
        getCurrentUserSteamId().let {
            _fetchUserSummaryState.value = true
            val result = fetchUserSummary(FetchUserSummaryUseCase.Params(it, reload))
            return when (result) {
                is Result.Success -> RequestState.success
                is Result.Error -> RequestState.Error(
                    resourceManager.getCommonErrorDescription(result.cause)
                )
            }.also {
                _fetchUserSummaryState.value = false
            }
        }
    }
}