package ru.luckycactus.steamroulette.presentation.roulette.options

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.games_filter.SavePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.presentation.common.Event
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate

class RouletteOptionsViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel() {

    val playTimeFilterData: LiveData<List<OptionsFilterAdapter.FilterUiModel>>
    val closeAction: LiveData<Unit>
        get() = _closeAction

    private val _closeAction = MutableLiveData<Unit>()

    private val observePlayTimeFilter = AppModule.observePlayTimeFilterUseCase
    private val savePlayTimeFilter = AppModule.savePlayTimeFilterUseCase

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

    }

    fun onPlayTimeFilterSelect(playTimeFilter: EnPlayTimeFilter) {
        savePlayTimeFilter(
            SavePlayTimeFilterUseCase.Params(
                userViewModelDelegate.currentUserSteamId!!,
                playTimeFilter
            )
        )
        viewModelScope.launch {
            delay(300)
            _closeAction.value = Unit
        }
    }

    private fun getPlayTimeFilterText(playTimeFilter: EnPlayTimeFilter) =
        //resourceManager.getString(
        when (playTimeFilter) {
            EnPlayTimeFilter.All -> "Все игры"
            EnPlayTimeFilter.NotPlayed -> "Игры, в которые я не играл"
            EnPlayTimeFilter.NotPlayedIn2Weeks -> "Игры, в которые я не играл в последние 2 недели"
        }
    //)
}