package ru.luckycactus.steamroulette.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.presentation.common.Event

class MainViewModel : ViewModel() {

    val screen: LiveData<Event<Screen>>
        get() = _screen

    private val _screen = MutableLiveData<Event<Screen>>()

    private val getSignedInUserSteamIdUseCase = AppModule.getCurrentUserSteamIdUseCase

    private val signOutUserUserCase = AppModule.signOutUserUserCase

    fun onColdStart() {
        if (getSignedInUserSteamIdUseCase() != null) {
            _screen.value = Event(Screen.Roulette)
        } else {
            _screen.value = Event(Screen.Login)
        }
    }

    fun onSignInSuccess() {
        _screen.value = Event(Screen.Roulette)
    }

    fun onExit() {
        _screen.value = Event(Screen.Login)
        //todo progress
        viewModelScope.launch {
            signOutUserUserCase()
        }
    }

    enum class Screen {
        Login,
        Roulette
    }

}