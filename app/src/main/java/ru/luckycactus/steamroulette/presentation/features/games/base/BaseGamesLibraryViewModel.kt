package ru.luckycactus.steamroulette.presentation.features.games.base

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.base.BaseViewModel

abstract class BaseGamesLibraryViewModel : BaseViewModel() {

    val games = Pager(
        PagingConfig(50),
        pagingSourceFactory = ::getGamesPagingSource
    ).flow.cachedIn(viewModelScope)

    abstract fun getGamesPagingSource(): PagingSource<Int, GameHeader>
}