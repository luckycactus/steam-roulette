package ru.luckycactus.steamroulette.presentation.features.roulette

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.core.RequestState
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.*
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.ObserveUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.utils.exhaustive
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.utils.startWith

class RouletteViewModel @ViewModelInject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    private val getOwnedGamesPagingList: GetOwnedGamesPagingListUseCase,
    observePlayTimeFilter: ObservePlaytimeFilterUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    observeUserSummary: ObserveUserSummaryUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setGamesShown: SetGamesShownUseCase,
    private val setAllGamesShown: SetAllGamesShownUseCase,
    private val resourceManager: ResourceManager,
    @AppCoScope private val appScope: CoroutineScope
) : BaseViewModel() {

    val userSummary = observeUserSummary().asLiveData()
    val games: LiveData<List<GameHeader>?>
    val itemRemoved: Flow<Int>
    val itemsInserted: Flow<Pair<Int, Int>>
    val contentState: LiveData<ContentState>
        get() = _contentState.distinctUntilChanged()
    val controlsAvailable: LiveData<Boolean>
        get() = _controlsAvailable.distinctUntilChanged()

    private val _gamesPagingList = MutableLiveData<PagingGameList?>()
    private val _contentState = MediatorLiveData<ContentState>()
    private val _controlsAvailable = MutableLiveData<Boolean>().startWith(true)
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
        currentUserPlayTimeFilter = observePlayTimeFilter()
            .asLiveData()
            .distinctUntilChanged()

        _contentState.addSource(userViewModelDelegate.fetchGamesState) {
            rouletteStateInvalidated = true
            syncRouletteState()
        }

        _contentState.addSource(currentUserPlayTimeFilter) {
            rouletteStateInvalidated = true
            syncRouletteState()
        }

        val hiddenGamesCountLiveData = observeHiddenGamesCount()
            .asLiveData()
            .distinctUntilChanged()

        _contentState.addSource(hiddenGamesCountLiveData) {
            if (it < hiddenGamesCount)
                rouletteStateInvalidated = true
            hiddenGamesCount = it
            syncRouletteState()
        }

        games = _gamesPagingList.map { it?.list }
        itemsInserted = _gamesPagingList
            .asFlow()
            .flatMapLatest { it?.itemsInsertedChannel?.consumeAsFlow() ?: emptyFlow() }
            .onEach {
                updateTopGame()
            }

        itemRemoved = _gamesPagingList
            .asFlow()
            .flatMapLatest { it?.itemRemovedChannel?.consumeAsFlow() ?: emptyFlow() }
            .onEach {
                updateTopGame()
            }

        observe(topGame, this::onTopGameUpdated)
    }

    fun onGameSwiped(hide: Boolean) {
        _gamesPagingList.value?.let {
            if (!it.isEmpty()) {
                val game = it.removeTop()
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
                    setAllGamesShown(SetAllGamesShownUseCase.Params(false))
                }
                setGamesShown(
                    SetGamesShownUseCase.Params(listOf(game.appId), true)
                )
            }
        }
    }

    private fun hideGame(game: GameHeader) {
        appScope.launch {
            setGamesHidden(
                SetGamesHiddenUseCase.Params(
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

        if (fetchGamesState == RequestState.Loading) {
            _contentState.value = ContentState.Loading
            return
        }

        getPagingListJob = viewModelScope.launch {
            _contentState.value = ContentState.Loading

            val result = getOwnedGamesPagingList(
                GetOwnedGamesPagingListUseCase.Params(
                    filter,
                    viewModelScope
                )
            )

            if (!isActive) return@launch

            when (result) {
                is GetOwnedGamesPagingListUseCase.Result.Success ->
                    renderUpdatePagedListSuccess(result)
                is GetOwnedGamesPagingListUseCase.Result.Fail -> renderUpdatePagedListFail(
                    fetchGamesState,
                    result
                )
            }.exhaustive
        }

    }

    private fun renderUpdatePagedListSuccess(result: GetOwnedGamesPagingListUseCase.Result.Success) {
        firstPreviouslyShownGameId = result.firstShownGameId
        _gamesPagingList.value = result.pagingList
        _contentState.value = if (!result.pagingList.isEmpty()) {
            result.pagingList.start()
            ContentState.Success
        } else {
            ContentState.Placeholder(
                resourceManager.getString(R.string.error_filtered_games_not_found),
                titleType = ContentState.TitleType.None,
                buttonType = ContentState.ButtonType.None
            )
        }
    }


    private fun renderUpdatePagedListFail(
        fetchGamesState: RequestState<*>,
        fail: GetOwnedGamesPagingListUseCase.Result.Fail
    ) {
        _contentState.value = when (fetchGamesState) {
            is RequestState.Error -> ContentState.Placeholder(
                message = fetchGamesState.message,
                titleType = ContentState.TitleType.Custom(
                    resourceManager.getString(
                        R.string.error_get_owned_games
                    )
                ),
                buttonType = ContentState.ButtonType.Default
            )
            else -> when (fail) {
                GetOwnedGamesPagingListUseCase.Result.Fail.NoOwnedGames -> {
                    ContentState.Placeholder(
                        message = resourceManager.getString(R.string.error_zero_games),
                        titleType = ContentState.TitleType.Custom("¯\\_(ツ)_/¯"),
                        buttonType = ContentState.ButtonType.Default
                    )
                }
                is GetOwnedGamesPagingListUseCase.Result.Fail.Error -> {
                    fail.cause.printStackTrace()
                    ContentState.Placeholder(
                        resourceManager.getCommonErrorDescription(fail.cause),
                        titleType = ContentState.TitleType.Custom(
                            resourceManager.getString(
                                R.string.error_get_owned_games
                            )
                        ),
                        buttonType = ContentState.ButtonType.Default
                    )
                }
            }
        }
    }
}