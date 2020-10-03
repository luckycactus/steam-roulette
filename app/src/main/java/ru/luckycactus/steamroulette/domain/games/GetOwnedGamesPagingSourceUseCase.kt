package ru.luckycactus.steamroulette.domain.games

import androidx.paging.PagingSource
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import javax.inject.Inject

@Reusable
class GetOwnedGamesPagingSourceUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<GetOwnedGamesPagingSourceUseCase.Params, PagingSource<Int, GameHeader>>() {

    override fun execute(params: Params): PagingSource<Int, GameHeader> {
        return gamesRepository.getOwnedGamesPagingSource(
            params.filter,
            params.searchQuery
        )
    }

    class Params(
        val filter: GamesFilter,
        val searchQuery: String? = null
    )
}