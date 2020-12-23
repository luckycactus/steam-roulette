package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteMaxHoursUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel

class RouletteFiltersViewModel @ViewModelInject constructor(
    private val observeRouletteFilter: ObserveRouletteFilterUseCase,
    private val observeRouletteMaxHours: ObserveRouletteMaxHoursUseCase,
    private val saveRouletteFilter: SaveRouletteFilterUseCase,
) : BaseViewModel() {

    suspend fun getCurrentPlaytimeFilter() =
        observeRouletteFilter().first().playtime

    suspend fun getCurrentMaxPlaytimeSetting() =
        observeRouletteMaxHours().first()

    fun onOkClick(playtimeFilter: PlaytimeFilter) {
        viewModelScope.launch {
            saveRouletteFilter(GamesFilter(playtime = playtimeFilter))
        }
    }
}