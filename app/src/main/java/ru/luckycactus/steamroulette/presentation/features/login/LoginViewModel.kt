package ru.luckycactus.steamroulette.presentation.features.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.login.LoginUseCase
import ru.luckycactus.steamroulette.domain.login.ValidateSteamIdInputUseCase
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper
import ru.luckycactus.steamroulette.presentation.utils.extensions.getCommonErrorDescription
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateSteamIdInput: ValidateSteamIdInputUseCase,
    private val login: LoginUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router,
    private val analytics: AnalyticsHelper
) : BaseViewModel() {
    val progressState: LiveData<Boolean>
        get() = _progressState

    val loginButtonAvailableState: LiveData<Boolean>
        get() = _loginButtonAvailableState

    val errorState: LiveData<String>
        get() = _errorState

    private val _progressState = MutableLiveData(false)
    private val _loginButtonAvailableState = MutableLiveData(false)
    private val _errorState = MutableLiveData<String>()

    fun onSteamIdConfirmed(id: String) {
        viewModelScope.launch {
            _progressState.value = true
            login(id.trim()).let {
                when (it) {
                    is LoginUseCase.Result.Success -> router.newRootScreen(Screens.Roulette)
                    is LoginUseCase.Result.Fail -> renderFail(it)
                }
                analytics.logLoginAttempt(it)
            }
            _progressState.value = false
        }
    }

    fun onSteamIdInputChanged(userId: String) {
        _loginButtonAvailableState.value = validateSteamIdInput(userId.trim())
    }

    private fun renderFail(fail: LoginUseCase.Result.Fail) {
        _errorState.value = with(resourceManager) {
            when (fail) {
                LoginUseCase.Result.Fail.InvalidSteamIdFormat ->
                    getString(R.string.error_invalid_steamid_format)
                is LoginUseCase.Result.Fail.VanityNotFound ->
                    getString(R.string.error_user_with_vanity_url_not_found)
                is LoginUseCase.Result.Fail.SteamIdNotFound ->
                    getString(R.string.error_user_with_steamid_not_found)
                is LoginUseCase.Result.Fail.Error -> {
                    fail.exception.printStackTrace()
                    getCommonErrorDescription(fail.exception)
                }
            }
        }
    }
}