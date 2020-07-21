package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.ClearHiddenGamesUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class RouletteOptionsViewModel @Inject constructor(
    observePlayTimeFilter: ObservePlaytimeFilterUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val clearHiddenGames: ClearHiddenGamesUseCase,
    private val resourceManager: ResourceManager,
    private val router: Router
) : BaseViewModel() {
    val playTimePrefValue: LiveData<String>
    val hiddenGamesCount: LiveData<Int>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    init {
        playTimePrefValue = observePlayTimeFilter()
            .map { getPlayTimeFilterText(it) }
            .asLiveData()

        hiddenGamesCount = observeHiddenGamesCount().asLiveData()
    }

    fun onClearHiddenGames() {
        viewModelScope.launch {
            clearHiddenGames()
            closeWithDelay()
        }
    }

    private fun closeWithDelay() {
        viewModelScope.launch {
            delay(CLOSE_DELAY)
            close()
        }
    }

    private fun close() {
        _closeAction.value = Unit
    }

    private fun getPlayTimeFilterText(playTimeFilter: PlaytimeFilter) =
        when (playTimeFilter) {
            PlaytimeFilter.All -> resourceManager.getString(R.string.playtime_pref_all)
            PlaytimeFilter.NotPlayed -> resourceManager.getString(R.string.playtime_pref_not_played)
            is PlaytimeFilter.Limited -> resourceManager.getQuantityString(
                R.plurals.playtime_pref_max_time_full_plurals,
                playTimeFilter.maxHours,
                playTimeFilter.maxHours
            )
        }

    fun onHiddenGamesClick() {
        router.navigateTo(Screens.HiddenGames)
        close()
    }

    companion object {
        private const val CLOSE_DELAY = 300L
    }
}