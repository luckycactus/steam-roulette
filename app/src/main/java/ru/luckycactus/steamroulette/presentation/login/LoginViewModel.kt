package ru.luckycactus.steamroulette.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.exception.InvalidSteamIdFormatException
import ru.luckycactus.steamroulette.domain.exception.SteamIdNotFoundException
import ru.luckycactus.steamroulette.domain.exception.VanityNotFoundException
import ru.luckycactus.steamroulette.presentation.Event
import ru.luckycactus.steamroulette.presentation.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.startWith

class LoginViewModel(
) : ViewModel() {

    private val validateSteamIdInputUseCase = AppModule.validateSteamIdInputUseCase
    private val signInUseCase = AppModule.signInUseCase
    private val resourceManager = AppModule.resourceManager

    val progressState: LiveData<Boolean>
        get() = _progressState

    val loginButtonAvailableState: LiveData<Boolean>
        get() = _loginButtonAvailableState

    val errorState: LiveData<String>
        get() = _errorState

    val signInSuccessEvent = MutableLiveData<Event<Unit?>>() //todo refactor navigation

    private val _progressState = MutableLiveData<Boolean>().startWith(false)
    private val _loginButtonAvailableState = MutableLiveData<Boolean>().startWith(false)
    private val _errorState = MutableLiveData<String>()

    fun onSteamIdConfirmed(id: String) {
        viewModelScope.launch {
            try {
                _progressState.value = true
                signInUseCase(id)
                signInSuccessEvent.value = Event(null)
            } catch (e: VanityNotFoundException) {
                _errorState.value = resourceManager.getString(R.string.user_with_vanity_url_not_found)
            } catch (e: InvalidSteamIdFormatException) {
                _errorState.value = resourceManager.getString(R.string.invalid_steamid_format)
            } catch (e: SteamIdNotFoundException) {
                _errorState.value = resourceManager.getString(R.string.user_with_steamid_not_found)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorState.value = getCommonErrorDescription(resourceManager, e)
            } finally {
                _progressState.value = false
            }
        }
    }


    fun onSteamIdInputChanged(userId: String) {
        _loginButtonAvailableState.value = validateSteamIdInputUseCase(userId)
    }
}