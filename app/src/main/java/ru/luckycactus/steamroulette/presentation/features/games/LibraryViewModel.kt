package ru.luckycactus.steamroulette.presentation.features.games

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.toStateFlow
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPagingSourceUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryMaxHoursUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.utils.newDebouncer
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.getPlaytimeFilterDescription
import ru.terrakok.cicerone.Router

class LibraryViewModel @ViewModelInject constructor(
    private val getOwnedGamesPagingSource: GetOwnedGamesPagingSourceUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    observeLibraryFilter: ObserveLibraryFilterUseCase,
    private val observeLibraryMaxHours: ObserveLibraryMaxHoursUseCase,
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    private val saveLibraryFilter: SaveLibraryFilterUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel() {

    val games: Flow<PagingData<GameHeader>>
    val selectedFilterText: LiveData<String?>
    val hasAnyFilters: LiveData<Boolean>
    val libraryFilter: LiveData<LibraryFilter>
    val filteredGamesCount: LiveData<Int>
    val maxHours: LiveData<Int>
        get() = _maxHours

    private val searchQueryFlow = MutableStateFlow<String?>(null)
    private val searchQueryDebouncer = viewModelScope.newDebouncer()
    private val savedGamesFilterFlow: StateFlow<GamesFilter>

    private val selectedLibraryFilterFlow = MutableStateFlow<LibraryFilter?>(null)
    private var selectedMaxHoursFlow = MutableStateFlow<Int?>(null)
    private val selectedGamesFilter = MediatorLiveData<GamesFilter>()
    private val _maxHours = MutableLiveData<Int>()

    init {
        games = combine(
            searchQueryFlow,
            observeLibraryFilter().distinctUntilChanged()
        ) { query, filter ->
            GetOwnedGamesPagingSourceUseCase.Params(filter, query)
        }.flatMapLatest {
            Pager(
                PagingConfig(50),
                pagingSourceFactory = { getOwnedGamesPagingSource(it) }
            ).flow.cachedIn(viewModelScope)
        }

        selectedGamesFilter.addSource(
            combine(
                selectedLibraryFilterFlow.filterNotNull(),
                selectedMaxHoursFlow.filterNotNull()
            ) { libraryFilter, maxHours ->
                mapLibraryFilterToGamesFilter(libraryFilter, maxHours)
            }.asLiveData().distinctUntilChanged()
        ) {
            selectedGamesFilter.value = it
        }

        savedGamesFilterFlow = observeLibraryFilter()
            .toStateFlow(viewModelScope, GamesFilter.empty())

        libraryFilter = selectedLibraryFilterFlow.filterNotNull().asLiveData()

        selectedFilterText = selectedGamesFilter.map {
            getGamesFilterText(it)
        }

        hasAnyFilters = selectedFilterText.map { !it.isNullOrBlank() }

        filteredGamesCount = selectedGamesFilter.switchMap {
            observeOwnedGamesCount(it).asLiveData()
        }

        viewModelScope.launch {
            val maxHours = observeLibraryMaxHours().first()
            _maxHours.value = maxHours
            selectedMaxHoursFlow.value = maxHours
            selectedLibraryFilterFlow.value =
                mapGamesFilterToLibraryFilter(observeLibraryFilter().first())
        }
    }

    fun onSearchQueryChanged(query: String?) {
        searchQueryDebouncer.debounce(300) {
            searchQueryFlow.value = query?.trim()
        }
    }

    fun unhide(selection: List<Int>) {
        viewModelScope.launch {
            setGamesHidden(
                SetGamesHiddenUseCase.Params(
                    selection,
                    false
                )
            )
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            setAllGamesHidden(
                SetAllGamesHiddenUseCase.Params(false)
            )
        }
    }

    fun onFilterSelectionChanged(filter: LibraryFilter) {
        selectedLibraryFilterFlow.value = filter
    }

    fun onMaxHoursChanged(maxHours: Int) {
        selectedMaxHoursFlow.value = maxHours
    }

    fun onFilterSheetClosingStarted() {
        saveSelectedFilter()
    }

    fun clearFilters() {
        selectedLibraryFilterFlow.value = LibraryFilter.All
        saveSelectedFilter()
    }

    private fun saveSelectedFilter() {
        val selectedFilter = selectedGamesFilter.value
        if (selectedFilter == null || savedGamesFilterFlow.value == selectedFilter)
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
}