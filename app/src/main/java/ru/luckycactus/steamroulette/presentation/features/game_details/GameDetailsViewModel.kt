package ru.luckycactus.steamroulette.presentation.features.game_details

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.domain.utils.exhaustive
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModelMapper
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.utils.getCommonErrorDescription
import ru.terrakok.cicerone.Router

class GameDetailsViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val gameDetailsUiModelMapper: GameDetailsUiModelMapper,
    private val resourceManager: ResourceManager,
    private val getGameStoreInfo: GetGameStoreInfoUseCase,
    private val router: Router
) : BaseViewModel() {
    val gameDetails: LiveData<List<GameDetailsUiModel>>
        get() = _gameDetails

    private val appId: Int
        get() = gameStoreInfo?.appId ?: initialGameHeader.appId

    private val initialGameHeader = savedStateHandle.get<GameHeader>(ARG_GAME)!!

    private val _gameDetails = MutableLiveData<List<GameDetailsUiModel>>()

    private var gameStoreInfo: GameStoreInfo? = null

    companion object {
        const val ARG_GAME = "ARG_GAME"
    }

    init {
        loadInfo(true)
    }

    fun onRetryClick() {
        loadInfo(false)
    }

    fun onStoreClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow(
                GameUrlUtils.storePage(appId),
                trySteamApp = true
            )
        )
    }

    fun onHubClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow(
                GameUrlUtils.hubPage(appId),
                trySteamApp = true
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

    fun resolvedGameHasDifferentId() =
        gameStoreInfo?.appId != null && gameStoreInfo?.appId != initialGameHeader.appId

    private fun loadInfo(tryCache: Boolean) {
        viewModelScope.launch {
            var gameStoreInfo = if (tryCache)
                getGameStoreInfo.getFromCache(initialGameHeader.appId)
            else null
            if (gameStoreInfo == null) {
                renderLoading()

                val result =
                    getGameStoreInfo(GetGameStoreInfoUseCase.Params(initialGameHeader.appId))
                when (result) {
                    is GetGameStoreInfoUseCase.Result.Success -> {
                        gameStoreInfo = result.data
                    }
                    is GetGameStoreInfoUseCase.Result.Fail -> renderError(result)
                }.exhaustive
            }
            gameStoreInfo?.let {
                this@GameDetailsViewModel.gameStoreInfo = gameStoreInfo
                _gameDetails.value = gameDetailsUiModelMapper.mapFrom(it)
            }
        }
    }

    private fun renderError(fail: GetGameStoreInfoUseCase.Result.Fail) {
        val errorMessage = with(resourceManager) {
            when (fail) {
                GetGameStoreInfoUseCase.Result.Fail.GameNotFound ->
                    getString(R.string.fail_steam_store_info)

                is GetGameStoreInfoUseCase.Result.Fail.Error -> {
                    fail.cause.printStackTrace()
                    getCommonErrorDescription(fail.cause)
                }
            }
        }
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
        GameDetailsUiModel.Header(initialGameHeader)
}