package ru.luckycactus.steamroulette.presentation.roulette

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.games.GetAllLocalOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.games.HideGameUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesClearUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.presentation.common.ContentState
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import javax.inject.Inject

class RouletteViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    private val getAllLocalOwnedGamesUseCase: GetAllLocalOwnedGamesUseCase,
    private val observePlayTimeFilter: ObservePlayTimeFilterUseCase,
    private val observeHiddenGamesClear: ObserveHiddenGamesClearUseCase,
    private val hideGame: HideGameUseCase,
    private val resourceManager: ResourceManager
) : ViewModel() {

    val games: LiveData<out List<OwnedGame>>
        get() = _games
    val contentState: LiveData<ContentState>
        get() = _contentState.distinctUntilChanged()
    val openUrlAction: LiveData<Event<String>>
        get() = _openUrlAction
    val controlsAvailable: LiveData<Boolean>
        get() = _controlsAvailable.distinctUntilChanged()
    val queueResetAction: LiveData<Event<Unit>>
        get() = _queueResetAction

    private val _games = MutableLiveData<MutableList<OwnedGame>>()
    private val _contentState = MediatorLiveData<ContentState>()
    private val _openUrlAction = MutableLiveData<Event<String>>()
    private val _controlsAvailable = MutableLiveData<Boolean>()
    private val _queueResetAction = MutableLiveData<Event<Unit>>()
    private val currentUserPlayTimeFilter: LiveData<EnPlayTimeFilter>

    private var gamesQueueJob: Job? = null
    private var isNextGameAllowed = true
    private var nextGameDelayJob: Job? = null
    private var gamesEnded = false

    init {
        currentUserPlayTimeFilter =
            userViewModelDelegate.observeCurrentUserSteamId().switchMap {
                observePlayTimeFilter(it).distinctUntilChanged()
            }

        _contentState.addSource(userViewModelDelegate.fetchGamesState) {
            refreshGames()
        }

        _contentState.addSource(currentUserPlayTimeFilter) {
            refreshGames()
        }

        _contentState.addSource(userViewModelDelegate.observeCurrentUserSteamId().switchMap {
            observeHiddenGamesClear(
                it
            )
        }) {
            refreshGames()
        }
    }

    fun onGameSwiped(hide: Boolean) {
        _games.value?.let {
            if (it.isNotEmpty()) {
                val game = it.removeAt(0)
                if (hide) {
                    GlobalScope.launch {
                        hideGame(
                            HideGameUseCase.Params(
                                userViewModelDelegate.currentUserSteamId,
                                game.appId
                            )
                        )
                    }
                }

                if (it.isEmpty()) {
                    gamesEnded = true
                    _contentState.value = ContentState.Placeholder(
                        resourceManager.getString(R.string.games_queue_ended),
                        titleType = ContentState.TitleType.None,
                        buttonType = ContentState.ButtonType.Custom(
                            resourceManager.getString(R.string.restart_queue)
                        )
                    )
                }
            }
        }
    }

    fun onRetryClick() {
        if (gamesEnded) {
            refreshGames()
        } else {
            userViewModelDelegate.fetchGames()
        }
    }

    fun onSteamInfoClick() {
        _games.value?.getOrNull(0)?.let {
            _openUrlAction.value = Event(it.storeUrl)
        }
    }

    fun onSwipeProgress(progress: Float) {
        _controlsAvailable.value = (progress == 0f)
    }

    fun onAdapterUpdatedAfterSwipe() {
        _controlsAvailable.value = true
    }

    private fun refreshGames() {
        val fetchGamesResult = userViewModelDelegate.fetchGamesState.value
        val filter = currentUserPlayTimeFilter.value

        gamesQueueJob?.cancel()
        nextGameDelayJob?.cancel()
        isNextGameAllowed = true
        gamesEnded = false

        if (fetchGamesResult == null || filter == null)
            return

        if (fetchGamesResult == Result.Loading) {
            _contentState.value = ContentState.Loading
        } else {
            gamesQueueJob = viewModelScope.launch {
                try {
                    _contentState.value = ContentState.Loading
                    val newGames = getAllLocalOwnedGamesUseCase(
                        GetAllLocalOwnedGamesUseCase.Params(
                            userViewModelDelegate.currentUserSteamId,
                            filter
                        )
                    ).toMutableList()
                    _games.value = newGames
                    if (isActive) {
                        if (!newGames.isNullOrEmpty()) {
                            _contentState.value = ContentState.Success
                        } else {
                            _contentState.value = ContentState.Placeholder(
                                resourceManager.getString(R.string.error_filtered_games_not_found),
                                titleType = ContentState.TitleType.None,
                                buttonType = ContentState.ButtonType.None
                            )
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        throw e
                    } else {
                        when {
                            fetchGamesResult is Result.Error -> _contentState.value =
                                ContentState.Placeholder(
                                    message = fetchGamesResult.message,
                                    titleType = ContentState.TitleType.Custom(
                                        resourceManager.getString(
                                            R.string.error_get_owned_games
                                        )
                                    ),
                                    buttonType = ContentState.ButtonType.Default
                                )
                            e is MissingOwnedGamesException -> _contentState.value =
                                ContentState.Placeholder(
                                    message = resourceManager.getString(R.string.error_zero_games),
                                    titleType = ContentState.TitleType.Custom("¯\\_(ツ)_/¯"),
                                    buttonType = ContentState.ButtonType.Default
                                )
                            else -> {
                                _contentState.value = ContentState.Placeholder(
                                    message = getCommonErrorDescription(resourceManager, e),
                                    titleType = ContentState.TitleType.Custom(
                                        resourceManager.getString(
                                            R.string.error_get_owned_games
                                        )
                                    ),
                                    buttonType = ContentState.ButtonType.Default
                                )
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun delayNextGame() {
        nextGameDelayJob?.cancel()
        isNextGameAllowed = false
        nextGameDelayJob = viewModelScope.launch {
            delay(300)
            isNextGameAllowed = true
        }
    }
}