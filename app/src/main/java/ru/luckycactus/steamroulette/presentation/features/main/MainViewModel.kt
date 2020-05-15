package ru.luckycactus.steamroulette.presentation.features.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.app.MigrateAppUseCase
import ru.luckycactus.steamroulette.domain.app.SyncGamesPeriodicJob
import ru.luckycactus.steamroulette.domain.common.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.common.toStateFlow
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.Result
import ru.luckycactus.steamroulette.domain.core.invoke
import ru.luckycactus.steamroulette.domain.games.ClearHiddenGamesUseCase
import ru.luckycactus.steamroulette.domain.games.FetchUserOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.login.SignOutUserUseCase
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
    private val signOutUser: SignOutUserUseCase,
    private val migrateApp: MigrateAppUseCase,
    private val clearHiddenGames: ClearHiddenGamesUseCase,
    private val syncGamesPeriodicJob: SyncGamesPeriodicJob,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel(), UserViewModelDelegate {
    override val isUserLoggedIn: Boolean
        get() = _nullableCurrentUserSteamIdFlow.value != null

    override val userSummary: LiveData<UserSummary>
    override val fetchGamesState: LiveData<Result<Unit>>
        get() = _fetchGamesState
    override val fetchUserSummaryState: LiveData<Boolean>
        get() = _fetchUserSummaryState
    override val currentUserSteamId: Flow<SteamId>

    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    private val _nullableCurrentUserSteamIdFlow: StateFlow<SteamId?>

    private val _fetchGamesState = MutableLiveData<Result<Unit>>()
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
                    syncGamesPeriodicJob.start()

                    userScope.launch {
                        fetchGames(false)
                    }
                    userScope.launch {
                        fetchUserSummary(false)
                    }
                } ?: run {
                    syncGamesPeriodicJob.stop()

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
            signOutUser()
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
                if (gamesState !is Result.Error) {
                    handleUserFetchError(userDeferred.await())
                }
            }
        }
    }

    private fun handleUserFetchError(result: Result<Unit>) {
        if (result is Result.Error) {
            _errorMessage.value = Event(
                "%s: %s".format(
                    resourceManager.getString(R.string.error_user_update),
                    result.message
                )
            )
        }
    }


    private fun handleGamesFetchError(result: Result<Unit>) {
        if (result is Result.Error) {
            _errorMessage.value = Event(
                "%s: %s".format(
                    resourceManager.getString(R.string.error_get_owned_games),
                    result.message
                )
            )
        }
    }

    private suspend fun fetchGames(reload: Boolean): Result<Unit> {
        getCurrentUserSteamId().let {
            _fetchGamesState.value = Result.Loading
            return try {
                fetchUserOwnedGames(FetchUserOwnedGamesUseCase.Params(it, reload))
                syncGamesPeriodicJob.start(true)
                Result.success.also { _fetchGamesState.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(
                    if (e is GetOwnedGamesPrivacyException)
                        resourceManager.getString(R.string.error_get_owned_games_privacy)
                    else {
                        e.printStackTrace()
                        getCommonErrorDescription(resourceManager, e)
                    }
                ).also { _fetchGamesState.value = it }
            }
        }
    }

    private suspend fun fetchUserSummary(reload: Boolean): Result<Unit> {
        getCurrentUserSteamId().let {
            return try {
                _fetchUserSummaryState.value = true
                fetchUserSummary(FetchUserSummaryUseCase.Params(it, reload))
                Result.success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(getCommonErrorDescription(resourceManager, e))
            } finally {
                _fetchUserSummaryState.value = false
            }
        }
    }
}