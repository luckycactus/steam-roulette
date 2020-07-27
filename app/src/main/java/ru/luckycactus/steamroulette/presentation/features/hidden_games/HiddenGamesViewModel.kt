package ru.luckycactus.steamroulette.presentation.features.hidden_games

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.GetHiddenGamesPagedListUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router

class HiddenGamesViewModel @ViewModelInject constructor(
    getHiddenGamesPagedList: GetHiddenGamesPagedListUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    private val router: Router
) : BaseViewModel() {

    val hiddenGames = getHiddenGamesPagedList()
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
