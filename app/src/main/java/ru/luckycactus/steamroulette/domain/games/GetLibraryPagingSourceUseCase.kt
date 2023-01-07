package ru.luckycactus.steamroulette.domain.games

import androidx.paging.PagingSource
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

class GetLibraryPagingSourceUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(
        filter: GamesFilter,
        searchQuery: String? = null
    ): PagingSource<Int, LibraryGame> {
        return gamesRepository.getLibraryPagingSource(filter, searchQuery)
    }
}