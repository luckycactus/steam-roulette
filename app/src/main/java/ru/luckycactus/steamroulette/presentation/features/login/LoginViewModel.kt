package ru.luckycactus.steamroulette.presentation.features.login

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.login.LoginUseCase
import ru.luckycactus.steamroulette.domain.login.ValidateSteamIdInputUseCase
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper
import ru.luckycactus.steamroulette.presentation.utils.extensions.getCommonErrorDescription
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateSteamIdInput: ValidateSteamIdInputUseCase,
    private val login: LoginUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router,
    private val analytics: AnalyticsHelper
) : BaseViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private val _errorMessages = Channel<String>(capacity = Channel.UNLIMITED)
    val errorMessages: Flow<String>
        get() = _errorMessages.receiveAsFlow()

    fun onSteamIdConfirmed(id: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            login(id.trim()).let {
                when (it) {
                    is LoginUseCase.Result.Success -> router.newRootScreen(Screens.Roulette())
                    is LoginUseCase.Result.Fail -> renderFail(it)
                }
                analytics.logLoginAttempt(it)
            }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }

    fun onSteamIdInputChanged(userId: String) {
        _state.update {
            it.copy(
                steamIdInput = userId,
                loginButtonEnabled = validateSteamIdInput(userId.trim())
            )
        }
    }

    private fun renderFail(fail: LoginUseCase.Result.Fail) {
        val message = with(resourceManager) {
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
        _errorMessages.trySend(message)
    }

    data class UiState(
        val steamIdInput: String = "",
        val loginButtonEnabled: Boolean = false,
        val isLoading: Boolean = false
    )
}