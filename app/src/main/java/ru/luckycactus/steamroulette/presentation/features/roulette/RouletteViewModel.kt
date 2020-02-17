package ru.luckycactus.steamroulette.presentation.features.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.Result
import ru.luckycactus.steamroulette.domain.common.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPagingList
import ru.luckycactus.steamroulette.domain.games.HideGameUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveResetHiddenGamesEventUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.utils.nullableSwitchMap
import ru.luckycactus.steamroulette.presentation.utils.startWith
import javax.inject.Inject

class RouletteViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    private val getOwnedGamesPagingList: GetOwnedGamesPagingList,
    private val observePlayTimeFilter: ObservePlaytimeFilterUseCase,
    private val observeResetHiddenGamesEvent: ObserveResetHiddenGamesEventUseCase,
    private val hideGame: HideGameUseCase,
    private val resourceManager: ResourceManager
) : ViewModel() {
    val games: LiveData<List<GameHeader>?>
    val itemRemoved: LiveData<Event<Int>?> //todo flow
        get() = _itemRemoved
    val itemsInserted: LiveData<Event<Pair<Int, Int>>?> //todo flow
    val contentState: LiveData<ContentState>
        get() = _contentState.distinctUntilChanged()
    val openUrlAction: LiveData<Event<String>>
        get() = _openUrlAction
    val controlsAvailable: LiveData<Boolean>
        get() = _controlsAvailable.distinctUntilChanged()

    private val _gamesPagingList = MutableLiveData<PagingGameList?>()
    private val _contentState = MediatorLiveData<ContentState>()
    private val _openUrlAction = MutableLiveData<Event<String>>()
    private val _controlsAvailable = MutableLiveData<Boolean>().startWith(true)
    private val _itemRemoved = MediatorLiveData<Event<Int>>()
    private val currentUserPlayTimeFilter: LiveData<PlaytimeFilter>

    private var getPagingListJob: Job? = null
    private var gamesEnded = false

    init {
        currentUserPlayTimeFilter =
            userViewModelDelegate.currentUserSteamId.switchMap {
                observePlayTimeFilter(it).distinctUntilChanged()
            }

        _contentState.addSource(userViewModelDelegate.fetchGamesState) {
            refreshGames()
        }

        _contentState.addSource(currentUserPlayTimeFilter) {
            refreshGames()
        }

        _contentState.addSource(userViewModelDelegate.currentUserSteamId.switchMap {
            observeResetHiddenGamesEvent(it)
        }) {
            refreshGames()
        }

        games = _gamesPagingList.map { it?.list }
        itemsInserted = _gamesPagingList.nullableSwitchMap { it.itemsInsertedLiveData }
        _itemRemoved.addSource(_gamesPagingList) {
            if (it == null) {
                _itemRemoved.value = null
            }
        }
    }

    fun onGameSwiped(hide: Boolean) {
        _gamesPagingList.value?.let {
            if (!it.isEmpty()) {
                val game = it.removeTop()
                _itemRemoved.value =
                    Event(0)
                if (hide) {
                    GlobalScope.launch {
                        hideGame(
                            HideGameUseCase.Params(
                                userViewModelDelegate.getCurrentUserSteamId(),
                                game.appId
                            )
                        )
                    }
                }

                if (it.gamesEnded()) {
                    gamesEnded = true
                    _contentState.value = ContentState.Placeholder(
                        resourceManager.getString(R.string.games_queue_ended),
                        titleType = ContentState.TitleType.None,
                        buttonType = ContentState.ButtonType.Custom(
                            resourceManager.getString(R.string.restart_queue)
                        )
                    )
                    _gamesPagingList.value = null
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

    fun onSwipeProgress(progress: Float) {
        _controlsAvailable.value = (progress == 0f)
    }

    fun onAdapterUpdatedAfterSwipe() {
        _controlsAvailable.value = true
    }

    private fun refreshGames() {
        val fetchGamesResult = userViewModelDelegate.fetchGamesState.value
        val filter = currentUserPlayTimeFilter.value

        getPagingListJob?.cancel()
        _gamesPagingList.value?.finish()
        _gamesPagingList.value = null

        gamesEnded = false

        if (fetchGamesResult == null || filter == null)
            return

        if (fetchGamesResult == Result.Loading) {
            _contentState.value = ContentState.Loading
        } else {
            getPagingListJob = viewModelScope.launch {
                try {
                    _contentState.value = ContentState.Loading

                    val pagingGameList = getOwnedGamesPagingList(
                        GetOwnedGamesPagingList.Params(
                            userViewModelDelegate.getCurrentUserSteamId(),
                            filter,
                            viewModelScope
                        )
                    )
                    _gamesPagingList.value = pagingGameList
                    if (isActive) {
                        if (!pagingGameList.isEmpty()) {
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
}