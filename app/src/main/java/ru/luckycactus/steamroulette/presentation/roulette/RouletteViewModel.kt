package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.user.GetSignedInUserUseCase
import ru.luckycactus.steamroulette.domain.user.IsUserSignedInUseCase
import ru.luckycactus.steamroulette.domain.user.UserSummary

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class RouletteViewModel : ViewModel() {

    val userSummary: LiveData<UserSummary>
        get() = _userSummary

    private val _userSummary = MutableLiveData<UserSummary>()

    private val getSignedInUserUseCase: GetSignedInUserUseCase = AppModule.getSignedInUserUseCase
    private val isUserSignedInUseCase: IsUserSignedInUseCase = AppModule.isUserSignedInUseCase

    init {
        viewModelScope.launch {
            if (isUserSignedInUseCase()) {
                loadUserSummary()
            } else {
                //todo
            }
        }
    }

    private suspend fun CoroutineScope.loadUserSummary() {
        getSignedInUserUseCase.getCacheThenRemote(this).consumeEach {
            _userSummary.value = it
        }
    }
}