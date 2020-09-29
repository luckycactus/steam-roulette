package ru.luckycactus.steamroulette.presentation.features.games

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPagingSourceUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.utils.newDebouncer
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router

class GamesLibraryViewModel @ViewModelInject constructor(
    private val getOwnedGamesPagingSourceUseCase: GetOwnedGamesPagingSourceUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    private val router: Router
) : BaseViewModel() {

    val hiddenGamesCount = observeHiddenGamesCount().asLiveData()

    private val searchQueryFlow = MutableStateFlow<String?>(null)
    private val searchQueryDebouncer = viewModelScope.newDebouncer()

    init {
        observe(hiddenGamesCount) {
            if (it == 0) {
                viewModelScope.launch {
                    delay(300)
                    router.backTo(Screens.Roulette)
                }
            }
        }
    }

    val games: Flow<PagingData<GameHeader>> = searchQueryFlow.flatMapLatest {
        Pager(
            PagingConfig(50),
            pagingSourceFactory = { getGamesPagingSource(searchQueryFlow.value) }
        ).flow.cachedIn(viewModelScope)
    }

    private fun getGamesPagingSource(searchQuery: String?): PagingSource<Int, GameHeader> {
        return getOwnedGamesPagingSourceUseCase(
            GetOwnedGamesPagingSourceUseCase.Params(
                searchQuery = searchQuery
            )
        )
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
}