package ru.luckycactus.steamroulette.presentation.main

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import ru.luckycactus.steamroulette.domain.exception.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.user.RefreshUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.UpdateUserAndGamesUseCase
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.first
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription

class MainFlowViewModel(
) : ViewModel(), UserViewModelDelegate {

    override val currentUserSteamId
        get() = userSummary.value?.steamId
    override val userSummary: LiveData<UserSummary?>
    override val refreshUserSummaryState: LiveData<Boolean>
        get() = _refreshUserSummaryState

    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage
    val logonCheckedAction: LiveData<Event<Unit>>
        get() = _logonCheckedAction


    private val _currentUserSteamId: LiveData<SteamId?>
    private val _errorMessage = MutableLiveData<Event<String>>()
    private val _logonCheckedAction = MutableLiveData<Event<Unit>>()
    private val _refreshUserSummaryState = MutableLiveData<Boolean>()

    private val observeCurrentUserSteamIdUseCase = AppModule.observeCurrentUserSteamIdUseCase
    private val observeCurrentUserSummaryUseCase = AppModule.observeCurrentUserSummaryUseCase
    private val refreshUserSummaryUseCase = AppModule.refreshUserSummaryUseCase
    private val updateUserAndGamesUseCase = AppModule.updateUserAndGamesUseCase

    private val resourceManager: ResourceManager = AppModule.resourceManager


    init {
        _currentUserSteamId = observeCurrentUserSteamIdUseCase()
        userSummary = observeCurrentUserSummaryUseCase()
    }

    override fun observeCurrentUserSteamId(): LiveData<SteamId?> {
        return _currentUserSteamId
    }


    fun coldStart() {
        observeCurrentUserSteamId().first {
            it?.let {
                _logonCheckedAction.value = Event(Unit)
                _refreshUserSummary(false)
            } ?: throw IllegalStateException("The user isn't logged on") //todo fallback
        }
    }

    override fun refreshUserAndGames() {
        viewModelScope.launch {
            currentUserSteamId?.let {
                _refreshUserSummaryState.value = true
                try {
                    updateUserAndGamesUseCase(
                        UpdateUserAndGamesUseCase.Params(it)
                    )
                } catch (e: UpdateUserAndGamesUseCase.UpdateException) {
                    _errorMessage.value = Event(
                        if (e.gamesUpdateException != null) {
                            val description =
                                if (e.gamesUpdateException is GetOwnedGamesPrivacyException)
                                    resourceManager.getString(R.string.check_your_privacy)
                                else getCommonErrorDescription(resourceManager, e)
                            "%s: %s".format(
                                resourceManager.getString(R.string.games_sync_failure),
                                description
                            )
                        } else {
                            "%s: %s".format(
                                resourceManager.getString(R.string.user_update_error),
                                getCommonErrorDescription(resourceManager, e)
                            )
                        }
                    )
                } finally {
                    _refreshUserSummaryState.value = false
                }
            }
        }
    }

    private fun _refreshUserSummary(reload: Boolean) {
        currentUserSteamId?.let {
            viewModelScope.launch {
                try {
                    refreshUserSummaryUseCase(
                        RefreshUserSummaryUseCase.Params(
                            it,
                            reload
                        )
                    )
                } catch (e: Exception) {
                    if (reload || (userSummary.value?.steamId != it)) {
                        _errorMessage.value = Event(
                            "%s: %s".format(
                                resourceManager.getString(R.string.user_update_error),
                                getCommonErrorDescription(resourceManager, e)
                            )
                        )
                    }
                }
            }
        } ?: throw IllegalStateException("The user isn't logged on")
    }
}
