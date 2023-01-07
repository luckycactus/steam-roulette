package ru.luckycactus.steamroulette.domain.games_filter

import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

class SaveRouletteFilterUseCase @Inject constructor(
    private val filtersRepository: RouletteFilterRepository
) {
    suspend operator fun invoke(params: GamesFilter) {
        filtersRepository.saveFilter(params)
    }
}