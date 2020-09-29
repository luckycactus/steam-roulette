package ru.luckycactus.steamroulette.domain.games

import androidx.paging.PagingSource
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

@Reusable
class GetOwnedGamesPagingSourceUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : UseCase<GetOwnedGamesPagingSourceUseCase.Params, PagingSource<Int, GameHeader>>() {

    override fun execute(params: Params): PagingSource<Int, GameHeader> {
        return gamesRepository.getOwnedGamesPagingSource(
            params.shown,
            params.hidden,
            params.playtimeFilter,
            params.searchQuery
        )
    }

    class Params(
        val shown: Boolean? = null,
        val hidden: Boolean? = null,
        val playtimeFilter: PlaytimeFilter? = null,
        val searchQuery: String? = null
    )
}