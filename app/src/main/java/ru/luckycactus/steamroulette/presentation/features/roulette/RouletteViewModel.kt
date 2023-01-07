package ru.luckycactus.steamroulette.presentation.features.roulette

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.utils.extensions.exhaustive
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.SwipeDirection
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper
import ru.luckycactus.steamroulette.presentation.utils.extensions.getCommonErrorDescription

class RouletteViewModel @AssistedInject constructor(
    @Assisted private val userViewModelDelegate: UserViewModelDelegate,
    private val getOwnedGamesPagingList: GetOwnedGamesPagingListUseCase,
    observeRouletteFilter: ObserveRouletteFilterUseCase,
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setGamesShown: SetGamesShownUseCase,
    private val setAllGamesShown: SetAllGamesShownUseCase,
    private val resourceManager: ResourceManager,
    @AppCoScope private val appScope: CoroutineScope,
    private val analytics: AnalyticsHelper
) : BaseViewModel() {

    val games: LiveData<List<GameHeader>?>
    val contentState: LiveData<ContentState>
        get() = _contentState.distinctUntilChanged()

    private val _gamesPagingList = MutableStateFlow<PagingGameList?>(null)
    private val _contentState = MutableLiveData<ContentState>()
    private val gamesFilter: StateFlow<GamesFilter?>
    private val topGame: Flow<GameHeader?>

    private var getPagingListJob: Job? = null
    private var allGamesShowed = false
    private var viewVisible = true
    private var rouletteStateInvalidated = false
    private var gameWasHidden = false

    // first game in list that was previously shown
    // when we get that game on top it means that all games are shown
    // so we must clear shown state for all games
    private var firstPreviouslyShownGameId: Int? = null

    init {
        gamesFilter = observeRouletteFilter()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

        viewModelScope.launch {
            gamesFilter.collect {
                invalidateRouletteState()
            }
        }
        viewModelScope.launch {
            userViewModelDelegate.fetchGamesState.collect {
                invalidateRouletteState()
            }
        }
        viewModelScope.launch {
            observeOwnedGamesCount(GamesFilter.onlyHidden())
                .distinctUntilChanged()
                .collect {
                    if (gameWasHidden) {
                        gameWasHidden = false
                    } else {
                        invalidateRouletteState()
                    }
                }
        }

        games = _gamesPagingList.map { it?.data }.asLiveData()

        topGame = _gamesPagingList
            .flatMapLatest { it?.topGameFlow ?: emptyFlow() }

        viewModelScope.launch {
            topGame.collect { onTopGameUpdated(it) }
        }
    }

    fun onGameSwiped(hide: Boolean) {
        if (hide) {
            analytics.logSelectContent("Game swipe", "left")
        } else {
            analytics.logSelectContent("Game swipe", "right")
        }

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
    }

    fun onRetryClick() {
        if (allGamesShowed) {
            invalidateRouletteState()
        } else {
            userViewModelDelegate.fetchGames()
        }
    }

    fun onHiddenChanged(hidden: Boolean) {
        viewVisible = !hidden
        syncRouletteState()
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
            gameWasHidden = true
            setGamesHidden(
                SetGamesHiddenUseCase.Params(
                    listOf(game.appId),
                    true
                )
            )
        }
    }

    private fun invalidateRouletteState() {
        rouletteStateInvalidated = true
        syncRouletteState()
    }

    private fun syncRouletteState() {
        if (!viewVisible)
            return

        if (!rouletteStateInvalidated)
            return

        val fetchGamesState = userViewModelDelegate.fetchGamesState.value
        val filter = gamesFilter.value

        getPagingListJob?.cancel()
        _gamesPagingList.value?.close()
        _gamesPagingList.value = null

        allGamesShowed = false
        rouletteStateInvalidated = false

        if (filter == null)
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
                    resourceManager.getString(R.string.error_get_owned_games)
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

    @AssistedFactory
    interface Factory {
        fun create(userViewModelDelegate: UserViewModelDelegate): RouletteViewModel
    }
}