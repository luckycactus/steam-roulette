package ru.luckycactus.steamroulette.presentation.features.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.exception.InvalidSteamIdFormatException
import ru.luckycactus.steamroulette.domain.exception.SteamIdNotFoundException
import ru.luckycactus.steamroulette.domain.exception.VanityNotFoundException
import ru.luckycactus.steamroulette.domain.login.SignInUseCase
import ru.luckycactus.steamroulette.domain.login.ValidateSteamIdInputUseCase
import ru.luckycactus.steamroulette.domain.common.Event
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.utils.startWith
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val validateSteamIdInputUseCase: ValidateSteamIdInputUseCase,
    private val signInUseCase: SignInUseCase,
    private val resourceManager: ResourceManager
) : ViewModel() {

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
                signInUseCase(id.trim())
                signInSuccessEvent.value =
                    Event(null)
            } catch (e: VanityNotFoundException) {
                _errorState.value =
                    resourceManager.getString(R.string.error_user_with_vanity_url_not_found)
            } catch (e: InvalidSteamIdFormatException) {
                _errorState.value = resourceManager.getString(R.string.error_invalid_steamid_format)
            } catch (e: SteamIdNotFoundException) {
                _errorState.value =
                    resourceManager.getString(R.string.error_user_with_steamid_not_found)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _errorState.value = getCommonErrorDescription(resourceManager, e)
            } finally {
                _progressState.value = false
            }
        }
    }

    fun onSteamIdInputChanged(userId: String) {
        _loginButtonAvailableState.value = validateSteamIdInputUseCase(userId.trim())
    }
}