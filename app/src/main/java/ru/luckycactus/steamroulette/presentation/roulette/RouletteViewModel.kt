package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import ru.luckycactus.steamroulette.domain.exception.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.games.FetchOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesQueueUseCase
import ru.luckycactus.steamroulette.domain.user.GetSignedInUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user.GetUserSummaryUseCase
import ru.luckycactus.steamroulette.presentation.getCommonErrorDescription

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class RouletteViewModel : ViewModel() {

    val userSummary: LiveData<UserSummary>
        get() = _userSummary
    val errorState: LiveData<String>
        get() = _errorState
    val currentGame: LiveData<OwnedGame>
        get() = _currentGame

    private val _userSummary = MutableLiveData<UserSummary>()
    private val _errorState = MutableLiveData<String>()
    private val _currentGame = MutableLiveData<OwnedGame>()

    private val getUserSummaryUseCase: GetUserSummaryUseCase = AppModule.getUserSummaryUseCase
    private val getSignedInUserSteamIdUseCase: GetSignedInUserSteamIdUseCase = AppModule.getSignedInUserSteamIdUseCase
    private val getOwnedGamesQueueUseCase: GetOwnedGamesQueueUseCase = AppModule.getOwnedGamesQueueUseCase

    private val resourceManager: ResourceManager = AppModule.resourceManager

    private lateinit var steamId: SteamId
    private lateinit var gamesQueue: OwnedGamesQueue

    init {
        getSignedInUserSteamIdUseCase()?.let {
            steamId = it
            loadUserSummary()
            getOwnedGamesQueue()
        } ?: userNotSignedInFallback()
    }

    fun onNextGameClick() {
        showNextGame()
    }

    fun onHideGameClick() {
        gamesQueue.markCurrentAsHidden()
        showNextGame()
    }

    private fun showNextGame() {
        if (gamesQueue.hasNext()) {
            viewModelScope.launch {//todo progress
                _currentGame.value = gamesQueue.next()
            }
        } else {
            //todo show error
        }
    }

    private fun loadUserSummary() {
        viewModelScope.launch {
            var userSummaryLoaded = false
            try {
                getUserSummaryUseCase.getCacheThenRemoteIfExpired(viewModelScope, steamId).consumeEach {
                    userSummaryLoaded = true
                    _userSummary.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!userSummaryLoaded) {
                    //shouldn't happen in normal environment
                    //todo error message
                    userNotSignedInFallback()
                }
            }
        }
    }

    private fun getOwnedGamesQueue() {
        viewModelScope.launch {
            try {
                gamesQueue = getOwnedGamesQueueUseCase(
                    GetOwnedGamesQueueUseCase.Params(
                        steamId,
                        false
                    )
                )
                showNextGame()
            } catch (e: GetOwnedGamesPrivacyException) {
                _errorState.value = resourceManager.getString(R.string.get_owned_games_exception_description)
            } catch (e: Exception) {
                _errorState.value = getCommonErrorDescription(resourceManager, e)
                e.printStackTrace()
            }
        }
    }

    private fun userNotSignedInFallback() {
        //todo
    }
}