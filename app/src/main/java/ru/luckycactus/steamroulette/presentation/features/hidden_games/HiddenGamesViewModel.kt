package ru.luckycactus.steamroulette.presentation.features.hidden_games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GetHiddenGamesPagedListUseCase
import ru.luckycactus.steamroulette.domain.games.SetGameHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import javax.inject.Inject

class HiddenGamesViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    private val getHiddenGamesPagedList: GetHiddenGamesPagedListUseCase,
    private val setGameHidden: SetGameHiddenUseCase
) : ViewModel() {
    val hiddenGames = userViewModelDelegate.currentUserSteamId.switchMap {
        getHiddenGamesPagedList(it)
    }

    fun onGameSwiped(gameHeader: GameHeader) {
        viewModelScope.launch {
            setGameHidden(
                SetGameHiddenUseCase.Params(
                    userViewModelDelegate.getCurrentUserSteamId(),
                    gameHeader.appId,
                    false
                )
            )
        }
    }
}
