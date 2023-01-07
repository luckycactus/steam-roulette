package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteMaxHoursUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class RouletteFiltersViewModel @Inject constructor(
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