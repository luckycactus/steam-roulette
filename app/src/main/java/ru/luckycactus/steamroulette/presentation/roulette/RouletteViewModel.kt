package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.games.GetLocalOwnedGamesQueueUseCase
import ru.luckycactus.steamroulette.presentation.common.ContentState
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.utils.nullableSwitchMap

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

    private val getLocalOwnedGamesQueue = AppModule.getOwnedGamesQueueUseCase
    private val observePlayTimeFilter = AppModule.observePlayTimeFilterUseCase
    private val observeHiddenGamesClear = AppModule.observeHiddenGamesClearUseCase

    private val resourceManager: ResourceManager = AppModule.resourceManager

    private var gamesQueue: OwnedGamesQueue? = null
    private var gamesQueueJob: Job? = null

    init {
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

    private fun refreshQueue() {
        val fetchGamesResult = userViewModelDelegate.fetchGamesState.value
        val filter = currentUserPlayTimeFilter.value

        gamesQueueJob?.cancel()
        gamesQueue?.finish()
        gamesQueue = null
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
                    if (fetchGamesResult is Result.Error)
                        _contentState.value =
                            ContentState.errorPlaceholder(fetchGamesResult.message)
                    else if (e is MissingOwnedGamesException) {
                        _contentState.value = ContentState.errorPlaceholder(
                            resourceManager.getString(R.string.you_dont_have_games_yet)
                        )
                    } else {
                        _contentState.value = ContentState.errorPlaceholder(
                            getCommonErrorDescription(resourceManager, e)
                        )
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun showNextGame() {
        gamesQueue?.let {
            if (it.hasNext()) {
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
                    resourceManager.getString(R.string.filtered_games_not_found),
                    titleType = ContentState.TitleType.None,
                    buttonType = ContentState.ButtonType.None
                )
            }
        }
    }

    fun onSteamInfoClick() {
        _currentGame.value?.let {
            _openUrlAction.value = Event(it.storeUrl)
        }
    }
}