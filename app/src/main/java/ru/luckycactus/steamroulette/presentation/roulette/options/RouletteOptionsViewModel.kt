package ru.luckycactus.steamroulette.presentation.roulette.options

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.AppModule
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.games.ClearHiddenGamesUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SavePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import javax.inject.Inject

class RouletteOptionsViewModel @Inject constructor(
    userViewModelDelegate: UserViewModelDelegate,
    observePlayTimeFilter: ObservePlayTimeFilterUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val savePlayTimeFilter: SavePlayTimeFilterUseCase,
    private val clearHiddenGames: ClearHiddenGamesUseCase,
    private val resourceManager: ResourceManager
) : ViewModel() {

    val playTimeFilterData: LiveData<List<OptionsFilterAdapter.FilterUiModel>>
    val hiddenGamesCount: LiveData<Int>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    private val userSteamId = userViewModelDelegate.currentUserSteamId

    init {
        playTimeFilterData =
            observePlayTimeFilter(userSteamId).map { checkedFilter ->
                EnPlayTimeFilter.values().asSequence()
                    .map {
                        OptionsFilterAdapter.FilterUiModel(
                            getPlayTimeFilterText(
                                it
                            ),
                            it == checkedFilter,
                            it
                        )
                    }.toList()
            }

        hiddenGamesCount = observeHiddenGamesCount(userSteamId)
    }

    fun onPlayTimeFilterSelect(playTimeFilter: EnPlayTimeFilter) {
        savePlayTimeFilter(
            SavePlayTimeFilterUseCase.Params(
                userSteamId,
                playTimeFilter
            )
        )
        closeWithDelay()
    }

    fun onClearHiddenGames() {
        //todo Что будет, если очистить во время обновления?
        viewModelScope.launch {
            clearHiddenGames(userSteamId)
        }
        closeWithDelay()
    }

    private fun closeWithDelay() {
        viewModelScope.launch {
            delay(CLOSE_DELAY)
            _closeAction.value = Unit
        }
    }

    private fun getPlayTimeFilterText(playTimeFilter: EnPlayTimeFilter) =
        resourceManager.getString(
            when (playTimeFilter) {
                EnPlayTimeFilter.All -> R.string.playtime_filter_all_games
                EnPlayTimeFilter.NotPlayed -> R.string.playtime_filter_not_played_games
                EnPlayTimeFilter.NotPlayedIn2Weeks -> R.string.playtime_filter_not_played_2_weeks_games
            }
        )

    companion object {
        private const val CLOSE_DELAY = 300L
    }
}