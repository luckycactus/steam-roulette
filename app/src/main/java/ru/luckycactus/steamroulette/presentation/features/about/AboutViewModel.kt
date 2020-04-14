package ru.luckycactus.steamroulette.presentation.features.about

import ru.luckycactus.steamroulette.domain.app.AppRepository
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class AboutViewModel @Inject constructor(
    appRepository: AppRepository,
    private val router: Router
) : BaseViewModel() {
    val version: String = "${appRepository.currentVersionName} (${appRepository.currentVersion})"

    fun onSourceCodeClick() {
        router.navigateTo(
            Screens.ExternalBrowserFlow("https://github.com/luckycactus/steam-roulette")
        )
    }

    fun onUsedLibrariesClick() {
        router.navigateTo(Screens.UsedLibraries)
    }
}