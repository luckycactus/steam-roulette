package ru.luckycactus.steamroulette.presentation.features.about

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import ru.luckycactus.steamroulette.domain.about.GetAppLibrariesUseCase
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AppLibrariesViewModel @Inject constructor(
    private val getAppLibraries: GetAppLibrariesUseCase,
    private val router: Router
) : BaseViewModel() {

    val libraries = flow {
        emit(getAppLibraries().sortedWith(compareBy(AppLibrary::author, AppLibrary::name)))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onLibraryClick(library: AppLibrary) {
        router.navigateTo(Screens.ExternalBrowserFlow(library.sourceUrl))
    }
}