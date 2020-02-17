package ru.luckycactus.steamroulette.presentation.features.game_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.GetGameStoreInfoException
import ru.luckycactus.steamroulette.domain.core.Event
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.presentation.common.ContentState
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModelMapper
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription

class GameDetailsViewModel @AssistedInject constructor(
    @Assisted private val gameHeader: GameHeader,
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
        loadInfo(true)
    }

    fun onRetryClick() {
        loadInfo(false)
    }

    fun onStoreClick() {
        //todo инжектить mainviewmodel и вызывать сразу у нее?
        _openUrlAction.value = Event(
            GameUrlUtils.storePage(resolvedAppId ?: gameHeader.appId)
        )
    }

    fun onHubClick() {
        _openUrlAction.value = Event(
            GameUrlUtils.hubPage(resolvedAppId ?: gameHeader.appId)
        )
    }

    private fun loadInfo(tryCache: Boolean) {
        viewModelScope.launch {
            var gameStoreInfo = if (tryCache)
                getGameStoreInfo.getFromCache(gameHeader.appId)
            else null
            if (gameStoreInfo == null) {
                renderLoading()
                try {
                    gameStoreInfo = getGameStoreInfo(
                        GetGameStoreInfoUseCase.Params(
                            gameHeader.appId,
                            false
                        )
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    renderError(e)
                }
            }
            if (gameStoreInfo != null) {
                resolvedAppId = gameStoreInfo.appId
                _gameDetails.value = gameDetailsUiModelMapper.mapFrom(gameStoreInfo)
            }
        }
    }

    private fun renderError(e: Exception) {
        val errorMessage = if (e is GetGameStoreInfoException)
            resourceManager.getString(R.string.fail_steam_store_info)
        else getCommonErrorDescription(resourceManager, e)
        val contentState = ContentState.errorPlaceholder(errorMessage)

        _gameDetails.value = listOf(
            getInitialHeader(),
            GameDetailsUiModel.DataLoading(contentState)
        )
    }

    private fun renderLoading() {
        _gameDetails.value = listOf(
            getInitialHeader(),
            GameDetailsUiModel.DataLoading(ContentState.Loading)
        )
    }


    private fun getInitialHeader(): GameDetailsUiModel =
        GameDetailsUiModel.Header(gameHeader)

    @AssistedInject.Factory
    interface Factory {
        fun create(gameHeader: GameHeader): GameDetailsViewModel
    }
}