package ru.luckycactus.steamroulette.presentation.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.di.LoginScreenModule
import ru.luckycactus.steamroulette.di.LoginScreenModule.validateSteamIdInputUseCase
import ru.luckycactus.steamroulette.domain.GetOwnedGamesUseCase
import ru.luckycactus.steamroulette.presentation.startWith

class LoginViewModel(
) : ViewModel() {

    private val validateSteamIdInputUseCase = LoginScreenModule.validateSteamIdInputUseCase
    private val signInUseCase = LoginScreenModule.signInUseCase

    val progressLiveData: LiveData<Boolean>
        get() = mutableProgressLiveData

    val loginButtonAvailableLiveData: LiveData<Boolean>
        get() = mutableLoginButtonStateLiveData

    private val mutableProgressLiveData = MutableLiveData<Boolean>().startWith(false)
    private val mutableLoginButtonStateLiveData = MutableLiveData<Boolean>().startWith(false)

    fun onSteamIdConfirmed(id: String) {
        viewModelScope.launch {
            try {
                mutableProgressLiveData.value = true
                Log.d("ololo", "" + signInUseCase(id).toString())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mutableProgressLiveData.value = false
            }
            //withContext(Dispatchers.IO) {
            //}
        }

    }

    fun onSteamSignInClick() {

    }

    fun onSteamIdInputChanged(userId: String) {
        mutableLoginButtonStateLiveData.value = validateSteamIdInputUseCase(userId)
    }


}