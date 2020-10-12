package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import javax.inject.Inject
import javax.inject.Named

@Reusable
class ObserveRouletteFilterUseCase @Inject constructor(
    @Named("roulette") private val rouletteFiltersRepository: GamesFilterRepository
) : UseCase<Unit, Flow<GamesFilter>>() {

    override fun execute(params: Unit): Flow<GamesFilter> =
        rouletteFiltersRepository.observeFilter(GamesFilter.empty())
}