package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlaytimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class RouletteOptionsViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate,
    observePlayTimeFilter: ObservePlaytimeFilterUseCase,
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
        playTimePrefValue = userViewModelDelegate.currentUserSteamId.switchMap {
            observePlayTimeFilter(it).map { getPlayTimeFilterText(it) }.asLiveData()
        }

        hiddenGamesCount = userViewModelDelegate.currentUserSteamId.switchMap {
            observeHiddenGamesCount(it).asLiveData()
        }
    }

    fun onClearHiddenGames() {
        userViewModelDelegate.resetHiddenGames()
        closeWithDelay()
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
                playTimeFilter.maxTime,
                playTimeFilter.maxTime
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