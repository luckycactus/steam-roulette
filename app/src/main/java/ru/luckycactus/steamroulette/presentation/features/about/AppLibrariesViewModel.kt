package ru.luckycactus.steamroulette.presentation.features.about

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.luckycactus.steamroulette.domain.about.GetAppLibrariesUseCase
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@HiltViewModel
class AppLibrariesViewModel @Inject constructor(
    private val getAppLibraries: GetAppLibrariesUseCase,
    private val router: Router
) : BaseViewModel() {

    suspend fun getLibraries() =
        getAppLibraries().sortedWith(compareBy(AppLibrary::author, AppLibrary::name))

    fun onLibraryClick(library: AppLibrary) {
        router.navigateTo(Screens.ExternalBrowserFlow(library.sourceUrl))
    }
}