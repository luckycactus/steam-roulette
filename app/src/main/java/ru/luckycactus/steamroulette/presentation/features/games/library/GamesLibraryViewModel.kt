package ru.luckycactus.steamroulette.presentation.features.games.library

import androidx.hilt.lifecycle.ViewModelInject
import androidx.paging.PagingSource
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesPagingSourceUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.games.base.BaseGamesLibraryViewModel

class GamesLibraryViewModel @ViewModelInject constructor(
    private val getOwnedGamesPagingSourceUseCase: GetOwnedGamesPagingSourceUseCase
) : BaseGamesLibraryViewModel() {

    override fun getGamesPagingSource(): PagingSource<Int, GameHeader> {
        return getOwnedGamesPagingSourceUseCase(GetOwnedGamesPagingSourceUseCase.Params())
    }
}