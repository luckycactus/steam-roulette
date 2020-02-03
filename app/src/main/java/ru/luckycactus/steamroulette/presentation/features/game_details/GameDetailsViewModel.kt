package ru.luckycactus.steamroulette.presentation.features.game_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.common.Event
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.exception.GetGameStoreInfoException
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameMinimal
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModelMapper
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription

class GameDetailsViewModel @AssistedInject constructor(
    @Assisted private val ownedGame: OwnedGame,
    private val gameDetailsUiModelMapper: GameDetailsUiModelMapper,
    private val resourceManager: ResourceManager,
    private val getGameStoreInfo: GetGameStoreInfoUseCase
) : ViewModel() {
    val gameDetails: LiveData<List<GameDetailsUiModel>>
        get() = _gameDetails
    val openUrlAction: LiveData<Event<String>>
        get() = _openUrlAction

    private val _gameDetails = MutableLiveData<List<GameDetailsUiModel>>()
    private val _openUrlAction = MutableLiveData<Event<String>>()

    private var resolvedAppId: Int? = null

    init {
        viewModelScope.launch {
            var gameStoreInfo = getGameStoreInfo.getFromCache(ownedGame.appId)
            if (gameStoreInfo == null) {
                renderInitialHeader()
                try {
                    gameStoreInfo = getGameStoreInfo(
                        GetGameStoreInfoUseCase.Params(
                            ownedGame.appId,
                            false
                        )
                    ).also {
                        resolvedAppId = it.appId
                    }

                } catch (e: GetGameStoreInfoException) {
                    e.printStackTrace()
                    //todo
                } catch (e: Exception) {
                    if (e is CancellationException)
                        throw e
                    else {
                        e.printStackTrace()
                        getCommonErrorDescription(resourceManager, e) //todo
                    }
                }
            }
            if (gameStoreInfo != null) {
                resolvedAppId = gameStoreInfo.appId
                _gameDetails.value = gameDetailsUiModelMapper.mapFrom(gameStoreInfo)
            }
        }
    }

    private fun renderInitialHeader() {
        _gameDetails.value = listOf<GameDetailsUiModel>(
                GameDetailsUiModel.Header(GameMinimal(ownedGame))
            )
    }

    fun onStoreClick() {
        //todo инжектить mainviewmodel и вызывать сразу у нее?
        _openUrlAction.value = Event(GameUrlUtils.storePage(resolvedAppId ?: ownedGame.appId))
    }

    fun onHubClick() {
        _openUrlAction.value = Event(GameUrlUtils.hubPage(resolvedAppId ?: ownedGame.appId))
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(ownedGame: OwnedGame): GameDetailsViewModel
    }
}