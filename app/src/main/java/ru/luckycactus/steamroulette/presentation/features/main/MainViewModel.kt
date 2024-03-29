package ru.luckycactus.steamroulette.presentation.features.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.RequestState
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.Result
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.UpdateOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.login.LogoutUserUseCase
import ru.luckycactus.steamroulette.domain.review.AppReviewManager
import ru.luckycactus.steamroulette.domain.user.FetchUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserUseCase
import ru.luckycactus.steamroulette.domain.user.ObserveCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper
import ru.luckycactus.steamroulette.presentation.utils.extensions.getCommonErrorDescription
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserSteamIdUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val fetchUserSummary: FetchUserSummaryUseCase,
    private val updateOwnedGames: UpdateOwnedGamesUseCase,
    private val logoutUser: LogoutUserUseCase,
    private val resourceManager: ResourceManager,
    private val appReviewManager: AppReviewManager,
    private val router: Router,
    private val analytics: AnalyticsHelper,
    @AppCoScope private val appScope: CoroutineScope
) : BaseViewModel(), UserViewModelDelegate {
    override val fetchGamesState: StateFlow<RequestState<Unit>>
        get() = _fetchGamesState
    override val fetchUserSummaryState: StateFlow<Boolean>
        get() = _fetchUserSummaryState

    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage
    val reviewRequest: LiveData<Event<Unit>>
        get() = _reviewRequest

    private val _fetchGamesState = MutableStateFlow<RequestState<Unit>>(RequestState.success)
    private val _fetchUserSummaryState = MutableStateFlow(true)
    private val _errorMessage = MutableLiveData<Event<String>>()
    private val _reviewRequest = MutableLiveData<Event<Unit>>()

    private var userScope: CoroutineScope? = null

    init {
        viewModelScope.launch {
            observeCurrentUser().collect {
                cancelAndJoinUserScope()

                it?.let {
                    userScope = viewModelScope + SupervisorJob(viewModelScope.coroutineContext[Job])

                    userScope!!.launch {
                        fetchGames(false)
                    }
                    userScope!!.launch {
                        fetchUserSummary(false)
                    }

                    appReviewManager.notifySessionStarted()
                }
            }
        }
    }

    fun onColdStart() {
        //clearImageCache()
        viewModelScope.launch {
            getCurrentUser().let {
                val screen = if (it != null) Screens.Roulette else Screens.Login
                router.newRootScreen(screen)
                if (it != null) {
                    if (appReviewManager.shouldRequestForReview()) {
                        delay(2000)
                        _reviewRequest.value = Event(Unit)
                    }
                }
            }
        }
    }

    fun onGameClick(game: GameHeader, color: Int, waitForImage: Boolean) {
        router.navigateTo(Screens.GameDetails(game, color, waitForImage))
    }

    fun onAppReviewed() {
        appReviewManager.setRated(true)
    }

    fun delayAppReview() {
        appReviewManager.delayReviewRequest()
    }

    fun disableAppReview() {
        appReviewManager.setReviewRequestsEnabled(false)
    }

    override fun logout() {
        viewModelScope.launch {
            cancelAndJoinUserScope()
            router.newRootScreen(Screens.Login)
            appScope.launch {
                analytics.setUserIsLoggingOut()
                logoutUser()
            }
        }
    }

    override fun fetchGames() {
        userScope!!.launch {
            handleGamesFetchError(fetchGames(true))
        }
    }

    override fun fetchUserAndGames() {
        userScope!!.launch {
            val userDeferred = userScope!!.async { fetchUserSummary(true) }
            val gamesState = fetchGames(true).also { handleGamesFetchError(it) }
            if (gamesState !is RequestState.Error) {
                handleUserFetchError(userDeferred.await())
            }
        }
    }

    private suspend fun cancelAndJoinUserScope() {
        userScope?.coroutineContext?.get(Job)?.cancelAndJoin()
        userScope = null
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
        _fetchGamesState.value = RequestState.Loading
        return when (val result = updateOwnedGames(UpdateOwnedGamesUseCase.Params(reload))) {
            is UpdateOwnedGamesUseCase.Result.Success -> {
                RequestState.success.also { _fetchGamesState.value = it }
            }
            is UpdateOwnedGamesUseCase.Result.Fail -> {
                val message = when (result) {
                    UpdateOwnedGamesUseCase.Result.Fail.PrivateProfile ->
                        resourceManager.getString(R.string.error_get_owned_games_privacy)
                    is UpdateOwnedGamesUseCase.Result.Fail.Error -> {
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

    private suspend fun fetchUserSummary(reload: Boolean): RequestState<Unit> {
        _fetchUserSummaryState.value = true
        val result = fetchUserSummary(FetchUserSummaryUseCase.Params(reload))
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