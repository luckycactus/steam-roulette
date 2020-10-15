package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

@Reusable
class SaveRouletteFilterUseCase @Inject constructor(
    private val filtersRepository: RouletteFilterRepository
) : SuspendUseCase<GamesFilter, Unit>() {

    override suspend fun execute(params: GamesFilter) {
        filtersRepository.saveFilter(params)
    }
}