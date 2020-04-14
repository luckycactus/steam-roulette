package ru.luckycactus.steamroulette.presentation.features.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.about.GetAppLibrariesUseCase
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.core.invoke
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class AppLibrariesViewModel @Inject constructor(
    private val getAppLibraries: GetAppLibrariesUseCase,
    private val router: Router
) : BaseViewModel() {

    val libraries: LiveData<List<AppLibrary>>
        get() = _libraries

    private val _libraries = MutableLiveData<List<AppLibrary>>()

    init {
        viewModelScope.launch {
            _libraries.value = getAppLibraries()
        }
    }

    fun onLibraryClick(library: AppLibrary) {
        router.navigateTo(Screens.ExternalBrowserFlow(library.sourceUrl))
    }
}