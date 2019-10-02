package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.games.GetLocalOwnedGamesQueueUseCase
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription

class RouletteViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel() {

    val currentGame: LiveData<OwnedGame>
        get() = _currentGame
    val contentState: LiveData<Result<Unit>>
        get() = _contentState

    private val _currentGame = MutableLiveData<OwnedGame>()
    private val _contentState = MediatorLiveData<Result<Unit>>()

    private val getLocalOwnedGamesQueueUseCase: GetLocalOwnedGamesQueueUseCase =
        AppModule.getOwnedGamesQueueUseCase

    private val resourceManager: ResourceManager = AppModule.resourceManager

    private var gamesQueue: OwnedGamesQueue? = null

    init {
        _contentState.addSource(userViewModelDelegate.fetchGamesState) {
            if (it == Result.Loading) {
                _contentState.value = it
                gamesQueue = null
            } else {
                viewModelScope.launch {
                    try {
                        gamesQueue =
                            getLocalOwnedGamesQueueUseCase(userViewModelDelegate.currentUserSteamId!!) //todo
                        showNextGame()
                        _contentState.value = Result.success
                    } catch (e: Exception) {
                        if (it is Result.Error)
                            _contentState.value = Result.Error(it.message)
                        else if (e is MissingOwnedGamesException) {
                            _contentState.value = Result.Error(
                                resourceManager.getString(R.string.you_dont_have_games_yet)
                            )
                        } else {
                            _contentState.value = Result.Error(
                                getCommonErrorDescription(resourceManager, e)
                            )
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun onNextGameClick() {
        showNextGame()
    }

    fun onHideGameClick() {
        gamesQueue?.markCurrentAsHidden()
        showNextGame()
    }

    fun onRetryClick() {
        userViewModelDelegate.fetchGames()
    }

    private fun showNextGame() {
        if (gamesQueue!!.hasNext()) { //todo
            viewModelScope.launch {
                //todo progress
                _currentGame.value = gamesQueue!!.next() //todo
            }
        } else {
            //todo show error
        }
    }


//    private fun getOwnedGamesQueue() {
//        viewModelScope.launch {
//            _contentState.value = MainFlowViewModel.ContentState.Loading
//            try {
//                //todo fallback to cache if remote failed
//                //todo use exist queue if failed
//                gamesQueue = getLocalOwnedGamesQueueUseCase(
//                    userViewModelDelegate.currentUserSteamId!!//todo
//                )
//                showNextGame()
//                _contentState.value = MainFlowViewModel.ContentState.Success
//            } catch (e: GetOwnedGamesPrivacyException) {
//                _contentState.value = MainFlowViewModel.ContentState.Error(
//                    resourceManager.getString(R.string.get_owned_games_exception_description)
//                )
//            } catch (e: MissingOwnedGamesException) {
//                _contentState.value = MainFlowViewModel.ContentState.Error(
//                    resourceManager.getString(R.string.you_dont_have_games_yet)
//                )
//            } catch (e: Exception) {
//                _contentState.value = MainFlowViewModel.ContentState.Error(
//                    getCommonErrorDescription(resourceManager, e)
//                )
//                e.printStackTrace()
//            }
//        }
//    }


}