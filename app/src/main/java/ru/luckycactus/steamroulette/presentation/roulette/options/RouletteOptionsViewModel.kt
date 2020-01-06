package ru.luckycactus.steamroulette.presentation.roulette.options

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.games.ClearHiddenGamesUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import javax.inject.Inject

class RouletteOptionsViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    observePlayTimeFilter: ObservePlaytimeFilterUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val clearHiddenGames: ClearHiddenGamesUseCase,
    private val resourceManager: ResourceManager
) : ViewModel() {

    val playTimePrefValue: LiveData<String>
    val hiddenGamesCount: LiveData<Int>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    init {
        playTimePrefValue = userViewModelDelegate.observeCurrentUserSteamId().switchMap {
            observePlayTimeFilter(it).map(this@RouletteOptionsViewModel::getPlayTimeFilterText)
        }

        hiddenGamesCount = userViewModelDelegate.observeCurrentUserSteamId()
            .switchMap { observeHiddenGamesCount(it) }
    }

    fun onClearHiddenGames() {
        //todo Что будет, если очистить во время обновления?
        viewModelScope.launch {
            clearHiddenGames(userViewModelDelegate.currentUserSteamId)
        }
        closeWithDelay()
    }

    private fun closeWithDelay() {
        viewModelScope.launch {
            delay(CLOSE_DELAY)
            _closeAction.value = Unit
        }
    }

    private fun getPlayTimeFilterText(playTimeFilter: PlaytimeFilter) =
        when (playTimeFilter) {
            PlaytimeFilter.All -> resourceManager.getString(R.string.playtime_pref_all)
            PlaytimeFilter.NotPlayed -> resourceManager.getString(R.string.playtime_pref_not_played)
            is PlaytimeFilter.Limited -> resourceManager.getQuantityString(
                R.plurals.playtime_pref_max_time_full_plurals,
                playTimeFilter.maxTime,
                playTimeFilter.maxTime
            )
        }

    companion object {
        private const val CLOSE_DELAY = 300L
    }
}