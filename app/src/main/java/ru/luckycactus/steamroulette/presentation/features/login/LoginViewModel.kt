package ru.luckycactus.steamroulette.presentation.features.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.login.SignInUseCase
import ru.luckycactus.steamroulette.domain.login.ValidateSteamIdInputUseCase
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.utils.startWith
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val validateSteamIdInputUseCase: ValidateSteamIdInputUseCase,
    private val signInUseCase: SignInUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel() {
    val progressState: LiveData<Boolean>
        get() = _progressState

    val loginButtonAvailableState: LiveData<Boolean>
        get() = _loginButtonAvailableState

    val errorState: LiveData<String>
        get() = _errorState

    private val _progressState = MutableLiveData<Boolean>().startWith(false)
    private val _loginButtonAvailableState = MutableLiveData<Boolean>().startWith(false)
    private val _errorState = MutableLiveData<String>()

    fun onSteamIdConfirmed(id: String) {
        viewModelScope.launch {
            _progressState.value = true
            signInUseCase(id.trim()).let {
                when (it) {
                    is SignInUseCase.Result.Success -> router.newRootScreen(Screens.Roulette)
                    is SignInUseCase.Result.Fail -> renderFail(it)
                }
            }
            _progressState.value = false
        }
    }

    private fun renderFail(fail: SignInUseCase.Result.Fail) {
        _errorState.value = with(resourceManager) {
            when (fail) {
                SignInUseCase.Result.Fail.InvalidSteamIdFormat ->
                    getString(R.string.error_invalid_steamid_format)
                SignInUseCase.Result.Fail.VanityNotFound ->
                    getString(R.string.error_user_with_vanity_url_not_found)
                SignInUseCase.Result.Fail.SteamIdNotFound ->
                    getString(R.string.error_user_with_steamid_not_found)
                is SignInUseCase.Result.Fail.Error -> {
                    fail.exception.printStackTrace()
                    getCommonErrorDescription(fail.exception)
                }
            }
        }
    }

    fun onSteamIdInputChanged(userId: String) {
        _loginButtonAvailableState.value = validateSteamIdInputUseCase(userId.trim())
    }
}