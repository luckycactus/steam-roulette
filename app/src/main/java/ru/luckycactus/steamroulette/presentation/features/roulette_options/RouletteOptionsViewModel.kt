package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router

class RouletteOptionsViewModel @ViewModelInject constructor(
    observeRouletteFilter: ObserveRouletteFilterUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
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
            .map { getPlayTimeFilterText(it) }
            .asLiveData()

        hiddenGamesCount = observeHiddenGamesCount().asLiveData()
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