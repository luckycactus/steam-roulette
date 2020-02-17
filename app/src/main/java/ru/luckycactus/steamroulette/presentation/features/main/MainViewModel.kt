package ru.luckycactus.steamroulette.presentation.features.main

import androidx.lifecycle.*
import kotlinx.coroutines.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.app.MigrateAppUseCase
import ru.luckycactus.steamroulette.domain.common.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.Result
import ru.luckycactus.steamroulette.domain.core.invoke
import ru.luckycactus.steamroulette.domain.games.FetchUserOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.login.SignOutUserUseCase
import ru.luckycactus.steamroulette.domain.user.FetchUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.ObserveCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user.ObserveUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.first
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import javax.inject.Inject

class MainViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserSteamIdUseCase,
    private val observeUserSummary: ObserveUserSummaryUseCase,
    private val fetchUserSummary: FetchUserSummaryUseCase,
    private val fetchUserOwnedGames: FetchUserOwnedGamesUseCase,
    private val signOutUser: SignOutUserUseCase,
    private val migrateApp: MigrateAppUseCase,
    private val resourceManager: ResourceManager
) : ViewModel(), UserViewModelDelegate {
    override val isUserLoggedIn: Boolean
        get() = _nullableCurrentUserSteamId.value != null

    override val currentUserSteamId: LiveData<SteamId>
        get() = _currentUserSteamId
    override val userSummary: LiveData<UserSummary>
    override val fetchGamesState: LiveData<Result<Unit>>
        get() = _fetchGamesState
    override val fetchUserSummaryState: LiveData<Boolean>
        get() = _fetchUserSummaryState

    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    val screen: LiveData<Event<Screen>>
        get() = _screen

    private val _currentUserSteamId = MediatorLiveData<SteamId>()
    private val _nullableCurrentUserSteamId: LiveData<SteamId?>

    private val _fetchGamesState = MutableLiveData<Result<Unit>>()
    private val _fetchUserSummaryState = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<Event<String>>()
    private val _screen = MutableLiveData<Event<Screen>>()

    init {
        _nullableCurrentUserSteamId = observeCurrentUser()
        _currentUserSteamId.addSource(_nullableCurrentUserSteamId) {
            viewModelScope.coroutineContext.cancelChildren()
            it?.let {
                _currentUserSteamId.value = it
                viewModelScope.launch {
                    fetchGames(false)
                }
                viewModelScope.launch {
                    fetchUserSummary(false)
                }
            }
        }
        userSummary = _currentUserSteamId.switchMap {
            observeUserSummary(it)
        }
        //todo
        currentUserSteamId.observeForever { }
    }

    fun onColdStart() {
        viewModelScope.launch {
            migrateApp()
            _nullableCurrentUserSteamId.first {
                val screen = if (it != null) Screen.Roulette else Screen.Login
                _screen.value =
                    Event(screen)
            }
        }
    }

    fun onSignInSuccess() {
        _screen.value =
            Event(Screen.Roulette)
    }

    fun onExit() {
        //todo progress
        viewModelScope.launch {
            signOutUser()
            _screen.value =
                Event(Screen.Login)
        }
    }

    override fun getCurrentUserSteamId(): SteamId {
        return _currentUserSteamId.value!!
    }

    override fun fetchGames() {
        viewModelScope.launch {
            handleGamesFetchError(fetchGames(true))
        }
    }

    override fun fetchUserAndGames() {
        viewModelScope.launch {
            supervisorScope {
                val userDeferred = viewModelScope.async { fetchUserSummary(true) }
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
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                } else {
                    Result.Error(getCommonErrorDescription(resourceManager, e))
                }
            } finally {
                _fetchUserSummaryState.value = false
            }
        }
    }

    //todo
    sealed class Screen {
        object Login : Screen()
        object Roulette : Screen()
        class GameDetails(val game: GameHeader) : Screen()
    }

}