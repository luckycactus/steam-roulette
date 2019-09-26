package ru.luckycactus.steamroulette.presentation.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegatePublic

class MenuViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel(), UserViewModelDelegatePublic by userViewModelDelegate {

    val gameCount: LiveData<Int>
    val gamesLastUpdate: LiveData<String>
        get() = _gamesLastUpdate

    private val _gamesLastUpdate = MutableLiveData<String>()

    private val observeOwnedGamesCount = AppModule.observeOwnedGamesCountUseCase

    fun refreshProfile() {
        userViewModelDelegate.refreshUserSummary()
    }

    init {
        gameCount = userViewModelDelegate.observeCurrentUserSteamId().switchMap {
            if (it != null)
                observeOwnedGamesCount(ObserveOwnedGamesCountUseCase.Params(it))
            else
                object : LiveData<Int>(0) {}
        }
    }
}