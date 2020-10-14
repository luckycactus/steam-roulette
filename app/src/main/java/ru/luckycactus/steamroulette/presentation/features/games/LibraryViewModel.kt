package ru.luckycactus.steamroulette.presentation.features.games

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.toStateFlow
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.*
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryMaxHoursUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.utils.newDebouncer
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.getPlaytimeFilterDescription
import ru.terrakok.cicerone.Router

class LibraryViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val getLibraryPagingSource: GetLibraryPagingSourceUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    private val getOwnedGameHiddenState: GetOwnedGameHiddenStateUseCase,
    observeLibraryFilter: ObserveLibraryFilterUseCase,
    private val observeLibraryMaxHours: ObserveLibraryMaxHoursUseCase,
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    private val saveLibraryFilter: SaveLibraryFilterUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel() {

    val games: Flow<PagingData<LibraryGame>>
    val selectedFilterText: LiveData<String?>
    val hasAnyFilters: LiveData<Boolean>
    val libraryFilter: LiveData<LibraryFilter>
    val filteredGamesCount: LiveData<Int>
    val maxHours: LiveData<Int>
        get() = _maxHours
    val hasSelectedHiddenGames: LiveData<Boolean>
    val filterChangedEvent: LiveData<Event<Unit>>
    val clearAllHiddenVisibility: LiveData<Boolean>
    val clearAllHiddenInFront: LiveData<Boolean>

    val onlyHidden = savedStateHandle.get<Boolean>(ARG_ONLY_HIDDEN) ?: false

    private val searchQueryFlow = MutableStateFlow<String?>(null)
    private val searchQueryDebouncer = viewModelScope.newDebouncer()
    private val appliedGamesFilterFlow: StateFlow<GamesFilter>

    private val libraryFilterFlow = MutableStateFlow<LibraryFilter?>(null)
    private var maxHoursFlow = MutableStateFlow<Int?>(null)
    private val selectedGamesFilter = MediatorLiveData<GamesFilter>()
    private val _maxHours = MutableLiveData<Int>()

    private val gameSelectionChangedChannel = Channel<Pair<Long, Boolean>>()

    init {
        val gamesFilterFlow = if (onlyHidden)
            flowOf(GamesFilter.onlyHidden())
        else
            observeLibraryFilter().distinctUntilChanged()

        appliedGamesFilterFlow = gamesFilterFlow
            .toStateFlow(viewModelScope, GamesFilter.empty())

        games = combine(
            searchQueryFlow,
            appliedGamesFilterFlow
        ) { query, filter ->
            GetLibraryPagingSourceUseCase.Params(filter, query)
        }.flatMapLatest {
            Pager(
                PagingConfig(50),
                pagingSourceFactory = { getLibraryPagingSource(it) }
            ).flow
        }.cachedIn(viewModelScope)

        selectedGamesFilter.addSource(
            combine(
                libraryFilterFlow.filterNotNull(),
                maxHoursFlow.filterNotNull()
            ) { libraryFilter, maxHours ->
                mapLibraryFilterToGamesFilter(libraryFilter, maxHours)
            }.asLiveData().distinctUntilChanged()
        ) {
            selectedGamesFilter.value = it
        }

        libraryFilter = libraryFilterFlow.filterNotNull().asLiveData()

        selectedFilterText = selectedGamesFilter.map {
            getGamesFilterText(it)
        }

        hasAnyFilters = selectedFilterText.map { !it.isNullOrBlank() }

        filteredGamesCount = selectedGamesFilter.switchMap {
            observeOwnedGamesCount(it).asLiveData()
        }

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

        filterChangedEvent = appliedGamesFilterFlow
            .asLiveData()
            .distinctUntilChanged()
            .map { Event(Unit) }

        val appliedLibraryFilterFlow = appliedGamesFilterFlow
            .map { mapGamesFilterToLibraryFilter(it) }

        clearAllHiddenInFront = appliedLibraryFilterFlow
            .map { it == LibraryFilter.Hidden }
            .asLiveData()

        val hiddenGamesCountFlow = observeOwnedGamesCount(GamesFilter.onlyHidden())

        clearAllHiddenVisibility = hiddenGamesCountFlow
            .map { it > 0 }
            .asLiveData()

        viewModelScope.launch {
            val maxHours = observeLibraryMaxHours().first()
            _maxHours.value = maxHours
            maxHoursFlow.value = maxHours
            libraryFilterFlow.value = mapGamesFilterToLibraryFilter(gamesFilterFlow.first())

            hiddenGamesCountFlow.collectLatest {
                if (it == 0 && libraryFilterFlow.value == LibraryFilter.Hidden) {
                    if (onlyHidden) {
                        router.backTo(Screens.Roulette)
                    } else {
                        onFilterSelectionChanged(LibraryFilter.All)
                        saveSelectedFilter()
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String?) {
        searchQueryDebouncer.debounce(300) {
            searchQueryFlow.value = query?.trim()
        }
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
        libraryFilterFlow.value = filter
    }

    fun onMaxHoursChanged(maxHours: Int) {
        maxHoursFlow.value = maxHours
    }

    fun onFilterSheetClosingStarted() {
        saveSelectedFilter()
    }

    fun clearFilters() {
        libraryFilterFlow.value = LibraryFilter.All
        saveSelectedFilter()
    }

    fun onGameSelectionChanged(appId: Long, selected: Boolean) {
        gameSelectionChangedChannel.offer(appId to selected)
    }

    private fun saveSelectedFilter() {
        val selectedFilter = selectedGamesFilter.value
        if (selectedFilter == null || appliedGamesFilterFlow.value == selectedFilter)
            return

        viewModelScope.launch {
            saveLibraryFilter(selectedFilter)
        }
    }

    private fun getGamesFilterText(filter: GamesFilter): String? {
        return when {
            filter.hidden == true -> resourceManager.getString(R.string.only_hidden)
            filter.playtime == PlaytimeFilter.All -> null
            else -> resourceManager.getPlaytimeFilterDescription(filter.playtime)
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
            hidden = if (libraryFilter == LibraryFilter.Hidden) true else null,
            playtime = playtimeFilter
        )
    }

    private fun mapGamesFilterToLibraryFilter(filter: GamesFilter): LibraryFilter {
        return when {
            filter.hidden == true -> LibraryFilter.Hidden
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
        const val ARG_ONLY_HIDDEN = "ARG_ONLY_HIDDEN"
    }
}