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
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModelMapper
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.terrakok.cicerone.Router

class GameDetailsViewModel @AssistedInject constructor(
    @Assisted private val gameHeader: GameHeader,
    private val gameDetailsUiModelMapper: GameDetailsUiModelMapper,
    private val resourceManager: ResourceManager,
    private val getGameStoreInfo: GetGameStoreInfoUseCase,
    private val router: Router
) : BaseViewModel() {
    val gameDetails: LiveData<List<GameDetailsUiModel>>
        get() = _gameDetails

    private val _gameDetails = MutableLiveData<List<GameDetailsUiModel>>()

    private var gameStoreInfo: GameStoreInfo? = null

    init {
        loadInfo(true)
    }

    fun onRetryClick() {
        loadInfo(false)
    }

    fun onStoreClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow(
                GameUrlUtils.storePage(gameStoreInfo?.appId ?: gameHeader.appId),
                true
            )
        )
    }

    fun onHubClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow(
                GameUrlUtils.hubPage(gameStoreInfo?.appId ?: gameHeader.appId),
                true
            )
        )
    }

    fun onMetacriticClick() {
        gameStoreInfo?.metacritic?.url?.let {
            router.navigateTo(Screens.ExternalBrowserFlow(it))
        }
    }

    fun onSystemRequirementsClick() {
        gameStoreInfo?.let {
            router.navigateTo(Screens.SystemReqs(it.name, it.requirements))
        }
    }

    fun onDetailedDescriptionClick() {
        gameStoreInfo?.let {
            router.navigateTo(Screens.DetailedDescription(it.name, it.detailedDescription))
        }
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
            gameStoreInfo?.let {
                this@GameDetailsViewModel.gameStoreInfo = gameStoreInfo
                _gameDetails.value = gameDetailsUiModelMapper.mapFrom(it)
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