package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.games_filter.ObserveMaximumPlayTimeSettingUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SaveMaxPlaytimeSettingUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SavePlayTimeFilterTypeUseCase
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import javax.inject.Inject

class PlaytimeViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    private val observePlaytimeFilter: ObservePlaytimeFilterUseCase,
    private val observeMaximumPlayTimeSetting: ObserveMaximumPlayTimeSettingUseCase,
    private val savePlayTimeFilterType: SavePlayTimeFilterTypeUseCase,
    private val saveMaxPlaytimeSetting: SaveMaxPlaytimeSettingUseCase
) : ViewModel() {
    val currentPlaytimeFilterType: LiveData<PlaytimeFilter.Type> by lazyNonThreadSafe {
        userViewModelDelegate.observeCurrentUserSteamId().switchMap { steamId ->
            observePlaytimeFilter(steamId).map { it.type }
        }
    }

    val currentMaxPlaytimeSetting: LiveData<Int> by lazyNonThreadSafe {
        userViewModelDelegate.observeCurrentUserSteamId().switchMap { steamId ->
            observeMaximumPlayTimeSetting(steamId)
        }
    }

    fun onOkClick(newFilterType: PlaytimeFilter.Type, newMaxPlaytime: Int) {
        viewModelScope.launch {
            if (newFilterType == PlaytimeFilter.Type.Limited) {
                saveMaxPlaytimeSetting(
                    SaveMaxPlaytimeSettingUseCase.Params(
                        userViewModelDelegate.currentUserSteamId,
                        newMaxPlaytime
                    )
                )
            }
            savePlayTimeFilterType(
                SavePlayTimeFilterTypeUseCase.Params(
                    userViewModelDelegate.currentUserSteamId,
                    newFilterType
                )
            )
        }
    }
}