package ru.luckycactus.steamroulette.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.common.AppModule
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.login.SignOutUserUseCase
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.common.Event
import javax.inject.Inject

class MainViewModel : ViewModel() {

    val screen: LiveData<Event<Screen>>
        get() = _screen

    //todo di
    @Inject lateinit var getSignedInUserSteamIdUseCase: GetCurrentUserSteamIdUseCase
    @Inject lateinit var signOutUserUserCase: SignOutUserUseCase

    private val _screen = MutableLiveData<Event<Screen>>()

    init {
        //todo di
        App.getInstance().appComponent().inject(this)
    }

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