package ru.luckycactus.steamroulette.presentation.features.about

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import ru.luckycactus.steamroulette.domain.app.AppRepository
import ru.luckycactus.steamroulette.domain.review.AppReviewManager
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    appRepository: AppRepository,
    appReviewManager: AppReviewManager,
    private val router: Router
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            version = "${appRepository.currentVersionName} (${appRepository.currentVersion})"
        )
    )
    val state = _state.asStateFlow()

    init {
        appReviewManager.observeRatedState()
            .onEach { rated ->
                _state.update {
                    it.copy(appRated = rated)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSourceCodeClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow(SOURCE_URL)
        )
    }

    fun onUsedLibrariesClick() {
        router.navigateTo(Screens.UsedLibraries())
    }

    fun onPrivacyPolicyClick() {
        router.navigateTo(Screens.ExternalBrowserFlow(PRIVACY_POLICY_URL))
    }

    fun contactDevViaSteam() {
        router.navigateTo(Screens.ExternalBrowserFlow(DEV_STEAM_PROFILE_URL))
    }

    data class UiState(
        val version: String = "",
        val appRated: Boolean = false
    )

    companion object {
        private const val DEV_STEAM_PROFILE_URL = "https://steamcommunity.com/id/luckycactus"
        private const val SOURCE_URL = "https://github.com/luckycactus/steam-roulette"
        private const val PRIVACY_POLICY_URL = "$SOURCE_URL/blob/master/PRIVACY_POLICY.md"
    }
}