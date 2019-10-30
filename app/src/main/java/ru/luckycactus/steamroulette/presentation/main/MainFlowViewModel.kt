package ru.luckycactus.steamroulette.presentation.main

import androidx.lifecycle.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import ru.luckycactus.steamroulette.domain.exception.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.games.FetchUserOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.user.FetchUserSummaryUseCase
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.first
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription

//todo Отдавать Result из UseCase?
class MainFlowViewModel(
) : ViewModel(), UserViewModelDelegate {

    override val currentUserSteamId
        get() = _currentUserSteamId.value!!
    override val userSummary: LiveData<UserSummary>
    override val fetchGamesState: LiveData<Result<Unit>>
        get() = _fetchGamesState
    override val fetchUserSummaryState: LiveData<Boolean>
        get() = _fetchUserSummaryState
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage
    val logonCheckedAction: LiveData<Event<Unit>>
        get() = _logonCheckedAction

    private val _currentUserSteamId = MediatorLiveData<SteamId>()
    private val _errorMessage = MutableLiveData<Event<String>>()
    private val _logonCheckedAction = MutableLiveData<Event<Unit>>()
    private val _fetchGamesState = MutableLiveData<Result<Unit>>()
    private val _fetchUserSummaryState = MutableLiveData<Boolean>()

    private val observeCurrentUser = AppModule.observeCurrentUserSteamIdUseCase
    private val observeUserSummary = AppModule.observeUserSummaryUseCase
    private val fetchUserSummary = AppModule.fetchUserSummaryUseCase
    private val fetchUserOwnedGames = AppModule.fetchUserOwnedGamesUseCase

    private val resourceManager: ResourceManager = AppModule.resourceManager

    init {
        _currentUserSteamId.addSource(observeCurrentUser()) {
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

    }

    fun coldStart() {
        _currentUserSteamId.first {
            _logonCheckedAction.value = Event(Unit)
        }
    }

    override fun observeCurrentUserSteamId() = _currentUserSteamId

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
        currentUserSteamId.let {
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
        currentUserSteamId.let {
            return try {
                _fetchUserSummaryState.value = true
                fetchUserSummary(
                    FetchUserSummaryUseCase.Params(
                        it,
                        reload
                    )
                )
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
}
