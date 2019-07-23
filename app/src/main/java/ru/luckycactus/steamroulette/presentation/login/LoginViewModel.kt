package ru.luckycactus.steamroulette.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.di.LoginScreenModule
import ru.luckycactus.steamroulette.di.LoginScreenModule.steamSignInUseCase
import ru.luckycactus.steamroulette.di.LoginScreenModule.validateSteamIdInputUseCase
import ru.luckycactus.steamroulette.domain.common.execute
import ru.luckycactus.steamroulette.domain.exception.InvalidSteamIdFormatException
import ru.luckycactus.steamroulette.domain.exception.SteamIdNotFoundException
import ru.luckycactus.steamroulette.domain.exception.VanityNotFoundException
import ru.luckycactus.steamroulette.presentation.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.startWith

class LoginViewModel(
) : ViewModel() {

    private val validateSteamIdInputUseCase = LoginScreenModule.validateSteamIdInputUseCase
    private val signInUseCase = LoginScreenModule.signInUseCase
    private val steamSignInUseCase = LoginScreenModule.steamSignInUseCase
    private val resourceManager = AppModule.resourceManager

    val progressLiveData: LiveData<Boolean>
        get() = mutableProgressLiveData

    val loginButtonAvailableLiveData: LiveData<Boolean>
        get() = mutableLoginButtonStateLiveData

    val errorLiveData: LiveData<String>
        get() = mutableErrorLiveData

    private val mutableProgressLiveData = MutableLiveData<Boolean>().startWith(false)
    private val mutableLoginButtonStateLiveData = MutableLiveData<Boolean>().startWith(false)
    private val mutableErrorLiveData = MutableLiveData<String>()

    fun onSteamIdConfirmed(id: String) {
        viewModelScope.launch {
            try {
                mutableProgressLiveData.value = true
                signInUseCase(id).toString()
                //todo change screen
            } catch (e: VanityNotFoundException) {
                mutableErrorLiveData.value = resourceManager.getString(R.string.user_with_vanity_url_not_found)
            } catch (e: InvalidSteamIdFormatException) {
                mutableErrorLiveData.value = resourceManager.getString(R.string.invalid_steamid_format)
            } catch (e: SteamIdNotFoundException) {
                mutableErrorLiveData.value = resourceManager.getString(R.string.user_with_steamid_not_found)
            } catch (e: Exception) {
                e.printStackTrace()
                mutableErrorLiveData.value = getCommonErrorDescription(resourceManager, e)
            } finally {
                mutableProgressLiveData.value = false
            }
        }
    }

    fun onSteamSignInClick() {
        steamSignInUseCase.execute()
    }

    fun onSteamIdInputChanged(userId: String) {
        mutableLoginButtonStateLiveData.value = validateSteamIdInputUseCase(userId)
    }
}