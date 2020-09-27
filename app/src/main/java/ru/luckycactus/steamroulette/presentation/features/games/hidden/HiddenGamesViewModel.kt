package ru.luckycactus.steamroulette.presentation.features.games.hidden

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPagingSourceUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveHiddenGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.SetAllGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.SetGamesHiddenUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.games.base.BaseGamesLibraryViewModel
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.terrakok.cicerone.Router

class HiddenGamesViewModel @ViewModelInject constructor(
    private val getOwnedGamesPagingSource: GetOwnedGamesPagingSourceUseCase,
    observeHiddenGamesCount: ObserveHiddenGamesCountUseCase,
    private val setGamesHidden: SetGamesHiddenUseCase,
    private val setAllGamesHidden: SetAllGamesHiddenUseCase,
    private val router: Router
) : BaseGamesLibraryViewModel() {

    val hiddenGamesCount = observeHiddenGamesCount().asLiveData()

    init {
        observe(hiddenGamesCount) {
            if (it == 0) {
                viewModelScope.launch {
                    delay(300)
                    router.backTo(Screens.Roulette)
                }
            }
        }
    }

    override fun getGamesPagingSource(): PagingSource<Int, GameHeader> =
        getOwnedGamesPagingSource(GetOwnedGamesPagingSourceUseCase.Params(hidden = true))

    fun unhide(selection: List<Int>) {
        viewModelScope.launch {
            setGamesHidden(
                SetGamesHiddenUseCase.Params(
                    selection,
                    false
                )
            )
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            setAllGamesHidden(
                SetAllGamesHiddenUseCase.Params(false)
            )
        }
    }
}
