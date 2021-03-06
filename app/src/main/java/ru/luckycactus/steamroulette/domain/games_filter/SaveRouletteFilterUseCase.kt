package ru.luckycactus.steamroulette.domain.games_filter

import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

class SaveRouletteFilterUseCase @Inject constructor(
    private val filtersRepository: RouletteFilterRepository
) : SuspendUseCase<GamesFilter, Unit>() {

    override suspend fun execute(params: GamesFilter) {
        filtersRepository.saveFilter(params)
    }
}