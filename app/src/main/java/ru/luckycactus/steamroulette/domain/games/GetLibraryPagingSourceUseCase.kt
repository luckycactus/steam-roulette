package ru.luckycactus.steamroulette.domain.games

import androidx.paging.PagingSource
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import javax.inject.Inject

@Reusable
class GetLibraryPagingSourceUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<GetLibraryPagingSourceUseCase.Params, PagingSource<Int, LibraryGame>>() {

    override fun execute(params: Params): PagingSource<Int, LibraryGame> {
        return gamesRepository.getLibraryPagingSource(
            params.filter,
            params.searchQuery
        )
    }

    class Params(
        val filter: GamesFilter,
        val searchQuery: String? = null
    )
}