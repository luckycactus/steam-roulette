package ru.luckycactus.steamroulette.presentation.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegatePublic

class MenuViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel(), UserViewModelDelegatePublic by userViewModelDelegate {

    val gameCount: LiveData<Int>
        get() = _gameCount
    val gamesLastUpdate: LiveData<String>
        get() = _gamesLastUpdate

    private val _gameCount = MutableLiveData<Int>()
    private val _gamesLastUpdate = MutableLiveData<String>()

    fun refreshUserSummary() {
        userViewModelDelegate.refreshUserSummary()
    }
}