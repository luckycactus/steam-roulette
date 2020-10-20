package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

@Reusable
class ObserveRouletteFilterUseCase @Inject constructor(
    private val filtersRepository: RouletteFilterRepository
) : UseCase<Unit, Flow<GamesFilter>>() {

    override fun execute(params: Unit): Flow<GamesFilter> =
        filtersRepository.observeFilter(GamesFilter.withoutHidden())
}