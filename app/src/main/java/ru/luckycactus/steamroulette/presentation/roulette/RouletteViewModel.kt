package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.user.GetSignedInUserUseCase
import ru.luckycactus.steamroulette.domain.user.IsUserSignedInUseCase
import ru.luckycactus.steamroulette.domain.user.UserSummary

class RouletteViewModel : ViewModel() {

    val userSummaryLiveData: LiveData<UserSummary>
        get() = mutableUserSummaryLiveData

    private val mutableUserSummaryLiveData = MutableLiveData<UserSummary>()

    private val getSignedInUserUseCase: GetSignedInUserUseCase = AppModule.getSignedInUserUseCase
    private val isUserSignedInUseCase: IsUserSignedInUseCase = AppModule.isUserSignedInUseCase

    init {
        viewModelScope.launch {
            if (isUserSignedInUseCase()) {
                mutableUserSummaryLiveData.value = getSignedInUserUseCase(GetSignedInUserUseCase.Params(false))
            } else {
                //todo
            }
        }
    }
}