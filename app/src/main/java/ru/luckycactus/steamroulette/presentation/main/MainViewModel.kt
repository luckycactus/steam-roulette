package ru.luckycactus.steamroulette.presentation.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.execute
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.user.IsUserSignedInUseCase
import ru.luckycactus.steamroulette.presentation.Event

class MainViewModel: ViewModel() {

    val screenLiveData = MutableLiveData<Event<Screen>>()

    private val isUserSignedInUseCase = AppModule.isUserSignedInUseCase

    private val signOutUserUserCase = AppModule.signOutUserUserCase

    fun onColdStart() {
        if (isUserSignedInUseCase()) {
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
        viewModelScope.launch {
            signOutUserUserCase()
            //todo почистить кэш репозитория и картинок
        }
    }

    enum class Screen {
        Login,
        Roulette
    }

}