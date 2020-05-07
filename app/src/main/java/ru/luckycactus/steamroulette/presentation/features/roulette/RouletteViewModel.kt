package ru.luckycactus.steamroulette.presentation.features.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.common.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.Result
import ru.luckycactus.steamroulette.domain.games.*
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegatePublic
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.utils.nullableSwitchMap
import ru.luckycactus.steamroulette.presentation.utils.startWith
import javax.inject.Inject

class RouletteViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    private val getOwnedGamesPagingList: GetOwnedGamesPagingListUseCase,
    private val observePlayTimeFilter: ObservePlaytimeFilterUseCase,
    private var observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setGamesShown: SetGamesShownUseCase,
    private val setAllGamesShown: SetAllGamesShownUseCase,
    private val resourceManager: ResourceManager,
    @ForApplication private val appScope: CoroutineScope
) : BaseViewModel(), UserViewModelDelegatePublic by userViewModelDelegate {

    val games: LiveData<List<GameHeader>?>
    val itemRemoved: LiveData<Event<Int>?> //todo flow
        get() = _itemRemoved
    val itemsInserted: LiveData<Event<Pair<Int, Int>>?> //todo flow
    val contentState: LiveData<ContentState>
        get() = _contentState.distinctUntilChanged()
    val controlsAvailable: LiveData<Boolean>
        get() = _controlsAvailable.distinctUntilChanged()

    private val _gamesPagingList = MutableLiveData<PagingGameList?>()
    private val _contentState = MediatorLiveData<ContentState>()
    private val _controlsAvailable = MutableLiveData<Boolean>().startWith(true)
    private val _itemRemoved = MediatorLiveData<Event<Int>>()
    private val currentUserPlayTimeFilter: LiveData<PlaytimeFilter>
    private val topGame = MediatorLiveData<GameHeader?>()

    private var getPagingListJob: Job? = null
    private var allGamesShowed = false
    private var viewVisible = true
    private var hiddenGamesCount = 0
    private var rouletteStateInvalidated = false

    // first game in list that was previously shown
    // when we get that game on top it means that all games are shown
    // so we must clear shown state for all games
    private var firstPreviouslyShownGameId: Int? = null

    init {
        currentUserPlayTimeFilter = userViewModelDelegate.currentUserSteamId.switchMap {
            observePlayTimeFilter(it).distinctUntilChanged().asLiveData()
        }

        _contentState.addSource(userViewModelDelegate.fetchGamesState) {
            rouletteStateInvalidated = true
            syncRouletteState()
        }

        _contentState.addSource(currentUserPlayTimeFilter) {
            rouletteStateInvalidated = true
            syncRouletteState()
        }

        val hiddenGamesCountLiveData = userViewModelDelegate.currentUserSteamId.switchMap {
            observeHiddenGamesCount(it).asLiveData()
        }

        _contentState.addSource(hiddenGamesCountLiveData) {
            if (it < hiddenGamesCount)
                rouletteStateInvalidated = true
            hiddenGamesCount = it
            syncRouletteState()
        }

        games = _gamesPagingList.map { it?.list }
        itemsInserted = _gamesPagingList.nullableSwitchMap { it.itemsInsertedLiveData }
        _itemRemoved.addSource(_gamesPagingList) {
            if (it == null) {
                _itemRemoved.value = null
            }
        }

        topGame.addSource(itemsInserted) {
            updateTopGame()
        }

        topGame.addSource(_itemRemoved) {
            updateTopGame()
        }

        observe(topGame, this::onTopGameUpdated)
    }

    fun onGameSwiped(hide: Boolean) {
        _gamesPagingList.value?.let {
            if (!it.isEmpty()) {
                val game = it.removeTop()
                _itemRemoved.value = Event(0)
                if (hide) {
                    hideGame(game)
                }
                if (it.isFinished()) {
                    allGamesShowed = true
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
        _controlsAvailable.value = true
    }

    fun onRetryClick() {
        if (allGamesShowed) {
            rouletteStateInvalidated = true
            syncRouletteState()
        } else {
            userViewModelDelegate.fetchGames()
        }
    }

    fun onSwipeProgress(progress: Float) {
        _controlsAvailable.value = (progress == 0f)
    }

    fun onHiddenChanged(hidden: Boolean) {
        viewVisible = !hidden
        syncRouletteState()
    }

    private fun updateTopGame() {
        val newTopGame = _gamesPagingList.value?.peekTop()
        if (topGame.value != newTopGame)
            topGame.value = newTopGame
    }

    private fun onTopGameUpdated(game: GameHeader?) {
        if (game != null) {
            appScope.launch {
                if (game.appId == firstPreviouslyShownGameId) {
                    setAllGamesShown(
                        SetAllGamesShownUseCase.Params(
                            userViewModelDelegate.getCurrentUserSteamId(),
                            false
                        )
                    )
                }
                setGamesShown(
                    SetGamesShownUseCase.Params(
                        userViewModelDelegate.getCurrentUserSteamId(),
                        listOf(game.appId),
                        true
                    )
                )
            }
        }
    }

    private fun hideGame(game: GameHeader) {
        appScope.launch {
            setGamesHidden(
                SetGamesHiddenUseCase.Params(
                    userViewModelDelegate.getCurrentUserSteamId(),
                    listOf(game.appId),
                    true
                )
            )
        }
    }

    private fun syncRouletteState() {
        if (!viewVisible)
            return

        if (!rouletteStateInvalidated)
            return

        val fetchGamesState = userViewModelDelegate.fetchGamesState.value
        val filter = currentUserPlayTimeFilter.value

        getPagingListJob?.cancel()
        _gamesPagingList.value?.close()
        _gamesPagingList.value = null

        allGamesShowed = false
        rouletteStateInvalidated = false

        if (fetchGamesState == null || filter == null)
            return

        if (fetchGamesState == Result.Loading) {
            _contentState.value = ContentState.Loading
        } else {
            getPagingListJob = viewModelScope.launch {
                try {
                    _contentState.value = ContentState.Loading

                    val (pagingGameList, firstShownGameId) = getOwnedGamesPagingList(
                        GetOwnedGamesPagingListUseCase.Params(
                            userViewModelDelegate.getCurrentUserSteamId(),
                            filter,
                            viewModelScope
                        )
                    )
                    firstPreviouslyShownGameId = firstShownGameId
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
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    when {
                        fetchGamesState is Result.Error -> _contentState.value =
                            ContentState.Placeholder(
                                message = fetchGamesState.message,
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