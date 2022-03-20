package ru.luckycactus.steamroulette.presentation.features.games

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.*
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryMaxHoursUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.library.GetLibraryScaleUseCase
import ru.luckycactus.steamroulette.domain.library.SaveLibraryScaleUseCase
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.extensions.getPlaytimeFilterShortDescription
import ru.terrakok.cicerone.Router
import javax.inject.Inject
import kotlin.time.milliseconds

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getLibraryPagingSource: GetLibraryPagingSourceUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    private val getOwnedGameHiddenState: GetOwnedGameHiddenStateUseCase,
    observeLibraryFilter: ObserveLibraryFilterUseCase,
    private val observeLibraryMaxHours: ObserveLibraryMaxHoursUseCase,
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    private val saveLibraryFilter: SaveLibraryFilterUseCase,
    private val getLibraryScale: GetLibraryScaleUseCase,
    private val saveLibraryScale: SaveLibraryScaleUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel() {

    val games: Flow<PagingData<LibraryGame>>
    val selectedFilterText: LiveData<String?>
    val hasAnyFilters: LiveData<Boolean>
    val libraryFilter: LiveData<LibraryFilter>
    val filteredGamesCount: LiveData<Int>
    val maxHours: LiveData<Int>
    val hasSelectedHiddenGames: LiveData<Boolean>
    val filterChangedEvent: LiveData<Event<Unit>>
    val clearAllHiddenVisibility: LiveData<Boolean>
    val menuItemsVisibility: LiveData<Boolean>
    val clearAllHiddenInFront: LiveData<Boolean>
    val spanCount: LiveData<Int>
        get() = _spanCount

    val onlyHidden = savedStateHandle.get<Boolean>(ARG_ONLY_HIDDEN) ?: false

    private val searchOpened = MutableStateFlow(false)
    private val searchQuery = MutableStateFlow<String?>(null)

    private val _libraryFilter = MutableStateFlow<LibraryFilter?>(null)
    private var _maxHours = MutableStateFlow<Int?>(null)
    private val selectedGamesFilter = MutableStateFlow<GamesFilter?>(null)
    private val appliedGamesFilter: StateFlow<GamesFilter>
    private val _spanCount = MutableLiveData<Int>()

    private val gameSelectionChangedChannel = Channel<Pair<Long, Boolean>>(Channel.BUFFERED)

    init {
        val gamesFilterFlow = if (onlyHidden)
            flowOf(GamesFilter.onlyHidden())
        else
            observeLibraryFilter()

        appliedGamesFilter = gamesFilterFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            GamesFilter.onlyVisible()
        )

        games = combine(
            appliedGamesFilter,
            searchQuery.debounce(300.milliseconds),
            GetLibraryPagingSourceUseCase::Params
        ).flatMapLatest {
            Pager(
                PagingConfig(50),
                pagingSourceFactory = { getLibraryPagingSource(it) }
            ).flow
        }.cachedIn(viewModelScope)

        viewModelScope.launch {
            combine(
                _libraryFilter.filterNotNull(),
                _maxHours.filterNotNull(),
                ::mapLibraryFilterToGamesFilter
            ).collect {
                selectedGamesFilter.value = it
            }
        }

        libraryFilter = _libraryFilter
            .filterNotNull()
            .asLiveData()

        selectedFilterText = selectedGamesFilter
            .filterNotNull()
            .map { getGamesFilterText(it) }
            .asLiveData()

        hasAnyFilters = selectedFilterText
            .map { !it.isNullOrBlank() }

        filteredGamesCount = selectedGamesFilter
            .filterNotNull()
            .flatMapLatest { observeOwnedGamesCount(it) }
            .asLiveData()

        hasSelectedHiddenGames = gameSelectionChangedChannel.receiveAsFlow()
            .scan(emptySet<Long>()) { set, event ->
                val hiddenGames = if (set is MutableSet) set else mutableSetOf()
                val (appId, selected) = event
                if (!selected) {
                    hiddenGames.remove(appId)
                } else if (getOwnedGameHiddenState(appId)) {
                    hiddenGames.add(appId)
                }
                hiddenGames
            }.map { it.isNotEmpty() }
            .asLiveData()

        filterChangedEvent = appliedGamesFilter
            .asLiveData()
            .distinctUntilChanged()
            .map { Event(Unit) }

        maxHours = _maxHours
            .filterNotNull()
            .take(1)
            .asLiveData()

        val appliedLibraryFilterFlow = appliedGamesFilter
            .map { mapGamesFilterToLibraryFilter(it) }

        clearAllHiddenInFront = appliedLibraryFilterFlow
            .map { it == LibraryFilter.Hidden }
            .asLiveData()

        val hiddenGamesCountFlow = observeOwnedGamesCount(GamesFilter.onlyHidden())

        clearAllHiddenVisibility = combine(
            hiddenGamesCountFlow,
            searchOpened
        ) { count, opened -> !opened && count > 0 }
            .asLiveData()

        menuItemsVisibility = searchOpened
            .map { !it }
            .asLiveData()

        viewModelScope.launch {
            val maxHours = observeLibraryMaxHours().first()
            _maxHours.value = maxHours
            _libraryFilter.value = mapGamesFilterToLibraryFilter(gamesFilterFlow.first())

            hiddenGamesCountFlow.collectLatest {
                if (it == 0 && _libraryFilter.value == LibraryFilter.Hidden) {
                    if (onlyHidden) {
                        router.backTo(Screens.Roulette)
                    } else {
                        onFilterSelectionChanged(LibraryFilter.All)
                        saveSelectedFilter()
                    }
                }
            }
        }

        viewModelScope.launch {
            _spanCount.value = getLibraryScale(SPAN_COUNT_SMALL)
        }
    }

    fun onSearchQueryChanged(query: String?) {
        searchQuery.value = query?.trim()
    }

    fun hide(selection: List<Int>, hide: Boolean) {
        viewModelScope.launch {
            setGamesHidden(
                SetGamesHiddenUseCase.Params(selection, hide)
            )
        }
    }

    fun clearAllHidden() {
        viewModelScope.launch {
            setAllGamesHidden(
                SetAllGamesHiddenUseCase.Params(false)
            )
        }
    }

    fun onFilterSelectionChanged(filter: LibraryFilter) {
        _libraryFilter.value = filter
    }

    fun onMaxHoursChanged(maxHours: Int) {
        _maxHours.value = maxHours
    }

    fun onFilterSheetClosingStarted() {
        saveSelectedFilter()
    }

    fun clearFilters() {
        _libraryFilter.value = LibraryFilter.All
        saveSelectedFilter()
    }

    fun onChangeScaleClick() {
        val newSpanCount =
            if (spanCount.value == SPAN_COUNT_SMALL)
                SPAN_COUNT_BIG
            else SPAN_COUNT_SMALL
        viewModelScope.launch { saveLibraryScale(newSpanCount) }
        _spanCount.value = newSpanCount
    }

    fun onGameSelectionChanged(appId: Long, selected: Boolean) {
        gameSelectionChangedChannel.trySend(appId to selected)
    }

    fun onSearchStateChanged(opened: Boolean) {
        searchOpened.value = opened
    }

    private fun saveSelectedFilter() {
        val selectedFilter = selectedGamesFilter.value
        if (selectedFilter == null || appliedGamesFilter.value == selectedFilter)
            return

        viewModelScope.launch {
            saveLibraryFilter(selectedFilter)
        }
    }

    private fun getGamesFilterText(filter: GamesFilter): String? {
        return when {
            filter.hidden == true -> resourceManager.getString(R.string.only_hidden)
            filter.playtime == PlaytimeFilter.All -> null
            else -> resourceManager.getPlaytimeFilterShortDescription(filter.playtime)
        }
    }

    private fun mapLibraryFilterToGamesFilter(
        libraryFilter: LibraryFilter,
        maxHours: Int
    ): GamesFilter {
        val playtimeFilter = when (libraryFilter) {
            LibraryFilter.All -> PlaytimeFilter.All
            LibraryFilter.Hidden -> PlaytimeFilter.All
            LibraryFilter.NotPlayed -> PlaytimeFilter.NotPlayed
            LibraryFilter.Limited -> PlaytimeFilter.Limited(maxHours)
        }
        return GamesFilter(
            hidden = libraryFilter == LibraryFilter.Hidden,
            playtime = playtimeFilter
        )
    }

    private fun mapGamesFilterToLibraryFilter(filter: GamesFilter): LibraryFilter {
        return when (filter.hidden) {
            true -> LibraryFilter.Hidden
            else -> when (filter.playtime) {
                PlaytimeFilter.All -> LibraryFilter.All
                PlaytimeFilter.NotPlayed -> LibraryFilter.NotPlayed
                is PlaytimeFilter.Limited -> LibraryFilter.Limited
            }
        }
    }

    enum class LibraryFilter {
        All, Hidden, NotPlayed, Limited
    }

    companion object {
        const val SPAN_COUNT_SMALL = 3
        const val SPAN_COUNT_BIG = 4
        const val ARG_ONLY_HIDDEN = "ARG_ONLY_HIDDEN"
    }
}