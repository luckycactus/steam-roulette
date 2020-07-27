package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games_filter.ObserveMaxPlaytimeSettingUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveMaxPlaytimeSettingUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SavePlayTimeFilterTypeUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel

class PlaytimeViewModel @ViewModelInject constructor(
    private val observePlaytimeFilter: ObservePlaytimeFilterUseCase,
    private val observeMaxPlaytimeSetting: ObserveMaxPlaytimeSettingUseCase,
    private val savePlayTimeFilterType: SavePlayTimeFilterTypeUseCase,
    private val saveMaxPlaytimeSetting: SaveMaxPlaytimeSettingUseCase
) : BaseViewModel() {

    suspend fun getCurrentPlaytimeFilterType() =
        observePlaytimeFilter().map { it.type }.first()

    suspend fun getCurrentMaxPlaytimeSetting() =
        observeMaxPlaytimeSetting().first()

    fun onOkClick(newFilterType: PlaytimeFilter.Type, newMaxPlaytime: Int) {
        viewModelScope.launch {
            if (newFilterType == PlaytimeFilter.Type.Limited) {
                saveMaxPlaytimeSetting(SaveMaxPlaytimeSettingUseCase.Params(newMaxPlaytime))
            }
            savePlayTimeFilterType(newFilterType)
        }
    }
}