package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.games.GetLocalOwnedGamesQueueUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesClearUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.common.ContentState
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import javax.inject.Inject

class RouletteViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel() {

    val currentGame: LiveData<OwnedGame>
        get() = _currentGame
    val contentState: LiveData<ContentState>
        get() = _contentState.distinctUntilChanged()
    val openUrlAction: LiveData<Event<String>>
        get() = _openUrlAction
    val controlsAvailable: LiveData<Boolean>
        get() = _controlsAvailable
    val queueResetAction: LiveData<Event<Unit>>
        get() = _queueResetAction

    val nextGame: OwnedGame?
        get() = gamesQueue?.peekNext()

    private val _currentGame = MutableLiveData<OwnedGame>()
    private val _contentState = MediatorLiveData<ContentState>()
    private val _openUrlAction = MutableLiveData<Event<String>>()
    private val _controlsAvailable = MutableLiveData<Boolean>()
    private val _queueResetAction = MutableLiveData<Event<Unit>>()
    private val currentUserPlayTimeFilter: LiveData<EnPlayTimeFilter>

    //todo di
    @Inject
    lateinit var getLocalOwnedGamesQueue: GetLocalOwnedGamesQueueUseCase
    @Inject
    lateinit var observePlayTimeFilter: ObservePlayTimeFilterUseCase
    @Inject
    lateinit var observeHiddenGamesClear: ObserveHiddenGamesClearUseCase
    @Inject
    lateinit var resourceManager: ResourceManager

    private var gamesQueue: OwnedGamesQueue? = null
    private var gamesQueueJob: Job? = null
    private var isNextGameAllowed = true
    private var nextGameDelayJob: Job? = null

    init {
        //todo di
        App.getInstance().appComponent().inject(this)

        currentUserPlayTimeFilter =
            userViewModelDelegate.observeCurrentUserSteamId().switchMap {
                observePlayTimeFilter(it).distinctUntilChanged()
            }

        _contentState.addSource(userViewModelDelegate.fetchGamesState) {
            refreshQueue()
        }

        _contentState.addSource(currentUserPlayTimeFilter) {
            refreshQueue()
        }

        _contentState.addSource(userViewModelDelegate.observeCurrentUserSteamId().switchMap {
            observeHiddenGamesClear(
                it
            )
        }) {
            refreshQueue()
        }
    }

    fun onNextGameClick() {
        showNextGame()
    }

    fun onHideGameClick() {
        gamesQueue?.markCurrentAsHidden()
        showNextGame()
    }

    fun onRetryClick() {
        val gamesQueue = gamesQueue
        if (gamesQueue != null && !gamesQueue.hasNext() && gamesQueue.size > 0) {
            refreshQueue()
        } else {
            userViewModelDelegate.fetchGames()
        }
    }

    fun onSteamInfoClick() {
        _currentGame.value?.let {
            _openUrlAction.value = Event(it.storeUrl)
        }
    }

    private fun refreshQueue() {
        val fetchGamesResult = userViewModelDelegate.fetchGamesState.value
        val filter = currentUserPlayTimeFilter.value

        gamesQueueJob?.cancel()
        gamesQueue?.finish()
        gamesQueue = null
        nextGameDelayJob?.cancel()
        isNextGameAllowed = true

        if (fetchGamesResult == null || filter == null)
            return

        if (fetchGamesResult == Result.Loading) {
            _contentState.value = ContentState.Loading
        } else {
            gamesQueueJob = viewModelScope.launch {
                try {
                    _contentState.value = ContentState.Loading
                    gamesQueue =
                        getLocalOwnedGamesQueue(
                            GetLocalOwnedGamesQueueUseCase.Params(
                                userViewModelDelegate.currentUserSteamId,
                                filter
                            )
                        )
                    if (isActive) {
                        showNextGame()
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

    private fun showNextGame() {
        if (!isNextGameAllowed)
            return

        gamesQueue?.let {
            if (it.hasNext()) {
                delayNextGame()
                viewModelScope.launch {
                    _controlsAvailable.value = false
                    try {
                        val firstGame = !it.started
                        val nextGame = it.next()
                        if (firstGame) {
                            _queueResetAction.value = Event(Unit)
                        }
                        _currentGame.value = nextGame
                        _contentState.value = ContentState.Success
                    } finally {
                        _controlsAvailable.value = true
                    }
                }
            } else if (it.size > 0) {
                _contentState.value = ContentState.Placeholder(
                    resourceManager.getString(R.string.games_queue_ended),
                    titleType = ContentState.TitleType.None,
                    buttonType = ContentState.ButtonType.Custom(
                        resourceManager.getString(R.string.restart_queue)
                    )
                )
            } else {
                _contentState.value = ContentState.Placeholder(
                    resourceManager.getString(R.string.error_filtered_games_not_found),
                    titleType = ContentState.TitleType.None,
                    buttonType = ContentState.ButtonType.None
                )
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