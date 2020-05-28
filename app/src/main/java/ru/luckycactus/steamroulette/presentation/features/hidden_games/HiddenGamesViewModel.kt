package ru.luckycactus.steamroulette.presentation.features.hidden_games

import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.requireSuccess
import ru.luckycactus.steamroulette.domain.core.usecase.successOr
import ru.luckycactus.steamroulette.domain.games.GetHiddenGamesPagedListUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.emptyLiveData
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

    val hiddenGames = userViewModelDelegate.currentUserSteamId.asLiveData().switchMap {
        getHiddenGamesPagedList(it)
    }

    val hiddenGamesCount = userViewModelDelegate.currentUserSteamId
        .flatMapLatest { observeHiddenGamesCount(it) }
        .asLiveData()

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
