package ru.luckycactus.steamroulette.presentation.roulette

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.exception.GetOwnedGamesPrivacyException
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesQueueUseCase
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription

class RouletteViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel() {

    val currentGame: LiveData<OwnedGame>
        get() = _currentGame
    val contentState: LiveData<ContentState>
        get() = _contentState

    private val _currentGame = MutableLiveData<OwnedGame>()
    private val _contentState = MutableLiveData<ContentState>()

    private val getOwnedGamesQueueUseCase: GetOwnedGamesQueueUseCase =
        AppModule.getOwnedGamesQueueUseCase

    private val resourceManager: ResourceManager = AppModule.resourceManager

    private lateinit var gamesQueue: OwnedGamesQueue

    init {
        getOwnedGamesQueue(false)
    }

    fun onNextGameClick() {
        showNextGame()
    }

    fun onHideGameClick() {
        gamesQueue.markCurrentAsHidden()
        showNextGame()
    }

    fun onRetryClick() {
        getOwnedGamesQueue(true)
    }

    private fun showNextGame() {
        if (gamesQueue.hasNext()) {
            viewModelScope.launch {
                //todo progress
                _currentGame.value = gamesQueue.next()
            }
        } else {
            //todo show error
        }
    }


    private fun getOwnedGamesQueue(reload: Boolean) {
        viewModelScope.launch {
            _contentState.value = ContentState.Loading
            try {
                //todo fallback to cache if remote failed
                //todo use exist queue if failed
                gamesQueue = getOwnedGamesQueueUseCase(
                    GetOwnedGamesQueueUseCase.Params(
                        userViewModelDelegate.currentUserSteamId!!,//todo
                        reload
                    )
                )
                showNextGame()
                _contentState.value = ContentState.Loaded
            } catch (e: GetOwnedGamesPrivacyException) {
                _contentState.value = ContentState.Error(
                    resourceManager.getString(R.string.get_owned_games_exception_description)
                )
            } catch (e: MissingOwnedGamesException) {
                _contentState.value = ContentState.Error(
                    resourceManager.getString(R.string.you_dont_have_games_yet)
                )
            } catch (e: Exception) {
                _contentState.value = ContentState.Error(
                    getCommonErrorDescription(resourceManager, e)
                )
                e.printStackTrace()
            }
        }
    }

    sealed class ContentState {
        object Loading : ContentState()
        data class Error(val message: String) : ContentState()
        object Loaded : ContentState()
    }
}