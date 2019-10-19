package ru.luckycactus.steamroulette.presentation.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.presentation.common.Event
import kotlin.system.measureTimeMillis

class MainViewModel: ViewModel() {

    val screenLiveData = MutableLiveData<Event<Screen>>()

    private val getSignedInUserSteamIdUseCase = AppModule.getSignedInUserSteamIdUseCase

    private val signOutUserUserCase = AppModule.signOutUserUserCase

    fun onColdStart() {
        if (getSignedInUserSteamIdUseCase() != null) {
            screenLiveData.value = Event(Screen.Roulette)
        } else {
            screenLiveData.value = Event(Screen.Login)
        }
    }

    fun onSignInSuccess() {
        screenLiveData.value = Event(Screen.Roulette)
    }

    fun onExit() {
        screenLiveData.value = Event(Screen.Login)
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