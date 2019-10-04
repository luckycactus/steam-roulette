package ru.luckycactus.steamroulette.presentation.roulette.options

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.games_filter.SavePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate

class RouletteOptionsViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel() {

    val playTimeFilterData: LiveData<List<OptionsFilterAdapter.FilterUiModel>>
    val hiddenGamesCount: LiveData<Int>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    private val observePlayTimeFilter = AppModule.observePlayTimeFilterUseCase
    private val savePlayTimeFilter = AppModule.savePlayTimeFilterUseCase
    private val observeHiddenGamesCount = AppModule.observeHiddenGamesCountUseCase

    private val resourceManager = AppModule.resourceManager

    init {
        playTimeFilterData =
            observePlayTimeFilter(userViewModelDelegate.currentUserSteamId!!).map { checkedFilter ->
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

        hiddenGamesCount = observeHiddenGamesCount(userViewModelDelegate.currentUserSteamId!!)
    }

    fun onPlayTimeFilterSelect(playTimeFilter: EnPlayTimeFilter) {
        savePlayTimeFilter(
            SavePlayTimeFilterUseCase.Params(
                userViewModelDelegate.currentUserSteamId!!,
                playTimeFilter
            )
        )
        viewModelScope.launch {
            delay(CLOSE_DELAY)
            _closeAction.value = Unit
        }
    }

    fun onClearHiddenGames() {
        //todo Что будет, если очистить во время обновления?
        userViewModelDelegate.clearHiddenGames()
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