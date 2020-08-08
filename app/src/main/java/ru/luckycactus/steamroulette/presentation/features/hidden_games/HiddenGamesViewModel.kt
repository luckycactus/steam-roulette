package ru.luckycactus.steamroulette.presentation.features.hidden_games

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.GetHiddenGamesPagingSourceUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router

class HiddenGamesViewModel @ViewModelInject constructor(
    getHiddenGamesPagingSource: GetHiddenGamesPagingSourceUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    private val router: Router
) : BaseViewModel() {

    val hiddenGames = Pager(
        PagingConfig(50),
        pagingSourceFactory = { getHiddenGamesPagingSource() }
    ).flow.cachedIn(viewModelScope)

    val hiddenGamesCount = observeHiddenGamesCount().asLiveData()

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
