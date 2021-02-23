package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.luckycactus.steamroulette.presentation.utils.extensions.getPlaytimeFilterDescription
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@HiltViewModel
class RouletteOptionsViewModel @Inject constructor(
    observeRouletteFilter: ObserveRouletteFilterUseCase,
    observeOwnedGamesCount: ObserveOwnedGamesCountUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel() {

    val playTimePrefValue: LiveData<String>
    val hiddenGamesCount: LiveData<Int>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    init {
        playTimePrefValue = observeRouletteFilter()
            .map { resourceManager.getPlaytimeFilterDescription(it.playtime) }
            .asLiveData()

        hiddenGamesCount = observeOwnedGamesCount(GamesFilter.onlyHidden()).asLiveData()
    }

    fun onHiddenGamesClick() {
        router.navigateTo(Screens.HiddenGames)
        close()
    }

    private fun close() {
        _closeAction.value = Unit
    }
}