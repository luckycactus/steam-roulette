package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

@Reusable
class ObserveRouletteFilterUseCase @Inject constructor(
    private val filtersRepository: RouletteFilterRepository
) {
    operator fun invoke(): Flow<GamesFilter> {
        return filtersRepository.observeFilter(GamesFilter.onlyVisible())
    }
}