package ru.luckycactus.steamroulette.presentation.features.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.login.SignOutUserUseCase
import ru.luckycactus.steamroulette.domain.update.MigrateAppUseCase
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.common.Event
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val getSignedInUserSteamId: GetCurrentUserSteamIdUseCase,
    private val signOutUser: SignOutUserUseCase,
    private val migrateApp: MigrateAppUseCase
) : ViewModel() {

    val screen: LiveData<Event<Screen>>
        get() = _screen

    private val _screen = MutableLiveData<Event<Screen>>()

    fun onColdStart() {
        viewModelScope.launch {
            migrateApp()
            if (getSignedInUserSteamId() != null) {
                _screen.value =
                    Event(Screen.Roulette)
            } else {
                _screen.value =
                    Event(Screen.Login)
            }
        }
    }

    fun onSignInSuccess() {
        _screen.value =
            Event(Screen.Roulette)
    }

    fun onExit() {
        _screen.value =
            Event(Screen.Login)
        //todo progress
        viewModelScope.launch {
            signOutUser()
        }
    }

    enum class Screen {
        Login,
        Roulette
    }

}