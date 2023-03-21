package ru.luckycactus.steamroulette.presentation.features.game_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.analytics.Analytics
import ru.luckycactus.steamroulette.domain.analytics.Events
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.domain.games.entity.Screenshot
import ru.luckycactus.steamroulette.domain.utils.extensions.cancellable
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModelMapper
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.utils.extensions.getCommonErrorDescription
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameDetailsUiModelMapper: GameDetailsUiModelMapper,
    private val resourceManager: ResourceManager,
    private val getGameStoreInfo: GetGameStoreInfoUseCase,
    private val router: Router,
    private val analytics: Analytics
) : BaseViewModel() {

    private val requestedGame = savedStateHandle.get<GameHeader>(ARG_GAME)!!

    val requestedId: Int = requestedGame.appId

    private val _state = MutableStateFlow(UiState(getInitialUiModel(), ContentState.Loading))
    val state = _state.asStateFlow()

    private val appId: Int
        get() = gameStoreInfo?.appId ?: requestedGame.appId


    private var gameStoreInfo: GameStoreInfo? = null

    init {
        loadInfo(true)
    }

    fun onRetryClick() {
        loadInfo(false)
    }

    fun onStoreClick() {
        analytics.track(Events.Click("Steam Store"))
        router.navigateTo(
            Screens.ExternalBrowserFlow(
                GameUrlUtils.storePage(appId),
                trySteamApp = true
            )
        )
    }

    fun onHubClick() {
        analytics.track(Events.Click("Steam Community"))
        router.navigateTo(
            Screens.ExternalBrowserFlow(
                GameUrlUtils.hubPage(appId),
                trySteamApp = true
            )
        )
    }

    fun onScreenshotClick(screenshot: Screenshot) {
        router.navigateTo(
            Screens.ImageViewer(
                gameStoreInfo!!.screenshots,
                gameStoreInfo!!.screenshots.indexOf(screenshot),
                { it.full },
                { it.thumbnail }
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
        renderLoading()
        viewModelScope.launch {
            val cachePolicy = if (tryCache) CachePolicy.CacheOrRemote else CachePolicy.Remote
            kotlin.runCatching {
                getGameStoreInfo(requestedGame.appId, cachePolicy)
            }.cancellable()
                .onSuccess { handleSuccess(it) }
                .onFailure { handleError(it) }
        }
    }


    private fun handleSuccess(game: GameStoreInfo?) {
        gameStoreInfo = game
        _state.value = if (game != null) {
            UiState(
                gameDetailsUiModelMapper.mapFrom(game),
                placeholder = null
            )
        } else {
            val contentState = ContentState.Placeholder(
                resourceManager.getString(R.string.fail_steam_store_info),
                ContentState.TitleType.DefaultError,
                ContentState.ButtonType.None
            )
            UiState(getInitialUiModel(), contentState)
        }
    }


    private fun handleError(throwable: Throwable) {
        Timber.e(throwable)
        val contentState = ContentState.Placeholder(
            resourceManager.getCommonErrorDescription(throwable),
            ContentState.TitleType.DefaultError,
            ContentState.ButtonType.Default
        )
        _state.value = UiState(getInitialUiModel(), contentState)
    }

    private fun renderLoading() {
        _state.value = UiState(getInitialUiModel(), ContentState.Loading)
    }

    private fun getInitialUiModel(): GameDetailsUiModel {
        return GameDetailsUiModel(requestedGame)
    }

    class UiState(
        val gameDetails: GameDetailsUiModel,
        val placeholder: ContentState? = null
    )

    companion object {
        const val ARG_GAME = "ARG_GAME"
    }
}
