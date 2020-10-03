package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteMaxHoursUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel

class RouletteFiltersViewModel @ViewModelInject constructor(
    private val observeRouletteFilter: ObserveRouletteFilterUseCase,
    private val observeRouletteMaxHours: ObserveRouletteMaxHoursUseCase,
    private val saveRouletteFilter: SaveRouletteFilterUseCase,
) : BaseViewModel() {

    suspend fun getCurrentPlaytimeFilterType() =
        observeRouletteFilter().map { it.type }.first()

    suspend fun getCurrentMaxPlaytimeSetting() =
        observeRouletteMaxHours().first()

    fun onOkClick(newFilterType: PlaytimeFilter.Type, newMaxPlaytime: Int) {
        val newPlaytime = when (newFilterType) {
            PlaytimeFilter.Type.All -> PlaytimeFilter.All
            PlaytimeFilter.Type.NotPlayed -> PlaytimeFilter.NotPlayed
            PlaytimeFilter.Type.Limited -> PlaytimeFilter.Limited(newMaxPlaytime)
        }
        val newFilter = GamesFilter(playtime = newPlaytime)
        viewModelScope.launch {
            saveRouletteFilter(newFilter)
        }
    }
}