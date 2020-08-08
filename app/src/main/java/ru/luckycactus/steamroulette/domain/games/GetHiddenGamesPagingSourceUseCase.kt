package ru.luckycactus.steamroulette.domain.games

import androidx.paging.PagingSource
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import javax.inject.Inject

@Reusable
class GetHiddenGamesPagingSourceUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : AbstractUseCase<Unit, PagingSource<Int, GameHeader>>() {
    override fun execute(params: Unit): PagingSource<Int, GameHeader> {
        return gamesRepository.getHiddenGamesPagingSource()
    }
}