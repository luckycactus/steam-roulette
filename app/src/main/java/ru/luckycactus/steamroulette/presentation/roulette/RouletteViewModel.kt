package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.invoke
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import ru.luckycactus.steamroulette.domain.games.FetchOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.user.GetSignedInUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user.GetUserSummaryUseCase
import java.lang.Exception
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class RouletteViewModel : ViewModel() {

    private lateinit var steamId: SteamId

    val userSummary: LiveData<UserSummary>
        get() = _userSummary

    private val _userSummary = MutableLiveData<UserSummary>()

    private val getUserSummaryUseCase: GetUserSummaryUseCase = AppModule.getUserSummaryUseCase
    private val fetchOwnedGamesUseCase: FetchOwnedGamesUseCase = AppModule.fetchOwnedGamesUseCase
    private val getSignedInUserSteamIdUseCase: GetSignedInUserSteamIdUseCase = AppModule.getSignedInUserSteamIdUseCase

    init {
        getSignedInUserSteamIdUseCase()?.let {
            steamId = it
            viewModelScope.launch {
                loadUserSummary()
            }
            viewModelScope.launch {
                fetchOwnedGames()
            }
        } ?: userNotSignedInFallback()
    }

    private suspend fun loadUserSummary()  {
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

    private suspend fun fetchOwnedGames() {
        //fetchOwnedGamesUseCase(FetchOwnedGamesUseCase.Params())

    }

    private fun userNotSignedInFallback() {
        //todo
    }
}