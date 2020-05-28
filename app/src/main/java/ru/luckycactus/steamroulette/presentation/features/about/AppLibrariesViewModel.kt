package ru.luckycactus.steamroulette.presentation.features.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import ru.luckycactus.steamroulette.domain.about.GetAppLibrariesUseCase
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class AppLibrariesViewModel @Inject constructor(
    private val getAppLibraries: GetAppLibrariesUseCase,
    private val router: Router
) : BaseViewModel() {

    val libraries: LiveData<List<AppLibrary>> = liveData {
        emit(getAppLibraries().sortedWith(compareBy(AppLibrary::author, AppLibrary::name)))
    }

    fun onLibraryClick(library: AppLibrary) {
        router.navigateTo(Screens.ExternalBrowserFlow(library.sourceUrl))
    }
}