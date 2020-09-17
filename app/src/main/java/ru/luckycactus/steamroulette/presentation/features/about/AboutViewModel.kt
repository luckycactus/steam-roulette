package ru.luckycactus.steamroulette.presentation.features.about

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.domain.app.AppRepository
import ru.luckycactus.steamroulette.domain.review.AppReviewManager
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router

class AboutViewModel @ViewModelInject constructor(
    appRepository: AppRepository,
    appReviewManager: AppReviewManager,
    private val router: Router
) : BaseViewModel() {
    val version: String = "${appRepository.currentVersionName} (${appRepository.currentVersion})"

    val appRated = appReviewManager.observeRatedState().asLiveData()

    fun onSourceCodeClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow(SOURCE_URL)
        )
    }

    fun onUsedLibrariesClick() {
        router.navigateTo(Screens.UsedLibraries)
    }

    fun onPrivacyPolicyClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow(PRIVACY_POLICY_URL)
        )
    }

    companion object {
        private const val SOURCE_URL = "https://github.com/luckycactus/steam-roulette"
        private const val PRIVACY_POLICY_URL = "$SOURCE_URL/blob/master/PRIVACY_POLICY.md"
    }
}