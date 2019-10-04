package ru.luckycactus.steamroulette.presentation.roulette

import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.OwnedGamesQueue
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.games.GetLocalOwnedGamesQueueUseCase
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.luckycactus.steamroulette.presentation.utils.nullableSwitchMap

class RouletteViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel() {

    val currentGame: LiveData<OwnedGame>
        get() = _currentGame
    val contentState: LiveData<Result<Unit>>
        get() = _contentState

    private val _currentGame = MutableLiveData<OwnedGame>()
    private val _contentState = MediatorLiveData<Result<Unit>>()

    private val currentUserPlayTimeFilter: LiveData<EnPlayTimeFilter?>

    private val getLocalOwnedGamesQueue = AppModule.getOwnedGamesQueueUseCase
    private val observePlayTimeFilter = AppModule.observePlayTimeFilterUseCase

    private val resourceManager: ResourceManager = AppModule.resourceManager

    private var gamesQueue: OwnedGamesQueue? = null
    private var gamesQueueJob: Job? = null

    init {
        currentUserPlayTimeFilter =
            userViewModelDelegate.observeCurrentUserSteamId().nullableSwitchMap {
                observePlayTimeFilter(it).distinctUntilChanged()
            }

        _contentState.addSource(userViewModelDelegate.fetchGamesState) {
            refreshQueue()
        }

        _contentState.addSource(currentUserPlayTimeFilter) {
            refreshQueue()
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

    private fun refreshQueue() {
        val fetchGamesResult = userViewModelDelegate.fetchGamesState.value
        val filter = currentUserPlayTimeFilter.value

        gamesQueueJob?.cancel()
        if (fetchGamesResult == null || filter == null)
            return

        if (fetchGamesResult == Result.Loading) {
            _contentState.value = fetchGamesResult
            gamesQueue = null
        } else {
            gamesQueueJob = viewModelScope.launch {
                try {
                    _contentState.value = Result.Loading
                    gamesQueue =
                        getLocalOwnedGamesQueue(
                            GetLocalOwnedGamesQueueUseCase.Params(
                                userViewModelDelegate.currentUserSteamId!!, //todo
                                filter
                            )
                        )
                    if (isActive) {
                        showNextGame()
                        _contentState.value = Result.success
                    }
                } catch (e: Exception) {
                    if (fetchGamesResult is Result.Error)
                        _contentState.value = Result.Error(fetchGamesResult.message)
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
//                gamesQueue = getLocalOwnedGamesQueue(
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