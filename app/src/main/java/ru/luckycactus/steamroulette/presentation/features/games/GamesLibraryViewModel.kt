package ru.luckycactus.steamroulette.presentation.features.games

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPagingSourceUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryMaxHoursUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.utils.newDebouncer
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router

class GamesLibraryViewModel @ViewModelInject constructor(
    private val getOwnedGamesPagingSource: GetOwnedGamesPagingSourceUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    observeLibraryFilter: ObserveLibraryFilterUseCase,
    observeLibraryMaxHours: ObserveLibraryMaxHoursUseCase,
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    private val saveLibraryFilter: SaveLibraryFilterUseCase,
    private val router: Router
) : BaseViewModel() {

    val currentFilterText: LiveData<String?>
    val currentFilter: LiveData<FilterType>
    val maxHours: LiveData<Int>
    val filteredGamesCount: LiveData<Int>

    private val searchQueryFlow = MutableStateFlow<String?>(null)
    private val searchQueryDebouncer = viewModelScope.newDebouncer()

    init {
        val currentGamesFilterFlow = observeLibraryFilter().distinctUntilChanged()

        currentFilter = currentGamesFilterFlow
            .map { mapFromGamesFilter(it) }
            .asLiveData()
            .distinctUntilChanged()

        maxHours = observeLibraryMaxHours()
            .asLiveData()
            .distinctUntilChanged()

        currentFilterText = currentGamesFilterFlow.map {
            getGamesFilterText(it)
        }.asLiveData()

        filteredGamesCount = currentGamesFilterFlow.flatMapLatest {
            observeOwnedGamesCount(it)
        }.asLiveData()

        //todo library
        ///hidden
//        viewModelScope.launch {
//            observeHiddenGamesCount().collectLatest {
//                if (it == 0) {
//                    viewModelScope.launch {
//                        delay(300)
//                        router.backTo(Screens.Roulette)
//                    }
//                }
//            }
//        }
    }

    private fun getGamesFilterText(filter: GamesFilter): String? {
        return filter.toString() //todo library
    }

    val games =
        combine(
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

    fun onFilterSelectionChanged(filter: FilterType) {
        //todo library TIME!!!
        val playtimeFilter = when (filter) {
            FilterType.All -> PlaytimeFilter.All
            FilterType.Hidden -> PlaytimeFilter.All
            FilterType.NotPlayed -> PlaytimeFilter.NotPlayed
            FilterType.Limited -> PlaytimeFilter.Limited(2)
        }
        val gamesFilter = GamesFilter(
            hidden = if (filter == FilterType.Hidden) true else null,
            playtime = playtimeFilter
        )

        viewModelScope.launch {
            saveLibraryFilter(gamesFilter)
        }
    }

    private fun mapFromGamesFilter(filter: GamesFilter): FilterType {
        return when {
            filter.hidden == true -> FilterType.Hidden
            else -> when (filter.playtime) {
                PlaytimeFilter.All -> FilterType.All
                PlaytimeFilter.NotPlayed -> FilterType.NotPlayed
                is PlaytimeFilter.Limited -> FilterType.Limited
            }
        }
    }

    enum class FilterType {
        All, Hidden, NotPlayed, Limited
    }
}