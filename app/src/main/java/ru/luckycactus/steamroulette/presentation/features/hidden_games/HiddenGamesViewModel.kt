package ru.luckycactus.steamroulette.presentation.features.hidden_games

import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GetHiddenGamesPagedListUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class HiddenGamesViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    private val getHiddenGamesPagedList: GetHiddenGamesPagedListUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    private val observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val router: Router
) : BaseViewModel() {
    val hiddenGames = userViewModelDelegate.currentUserSteamId.switchMap {
        getHiddenGamesPagedList(it)
    }

    init {
        observe(observeHiddenGamesCount(userViewModelDelegate.getCurrentUserSteamId())) {
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
                    userViewModelDelegate.getCurrentUserSteamId(),
                    selection,
                    false
                )
            )
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            setAllGamesHidden(
                SetAllGamesHiddenUseCase.Params(
                    userViewModelDelegate.getCurrentUserSteamId(),
                    false
                )
            )
        }
    }
}
