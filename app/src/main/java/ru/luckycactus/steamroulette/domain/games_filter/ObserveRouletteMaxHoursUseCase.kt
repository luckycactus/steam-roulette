package ru.luckycactus.steamroulette.domain.games_filter

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.Consts
import javax.inject.Inject

class ObserveRouletteMaxHoursUseCase @Inject constructor(
    private val filtersRepository: RouletteFilterRepository
) {
    operator fun invoke(): Flow<Int> {
        return filtersRepository.observeMaxHours(Consts.DEFAULT_MAX_HOURS)
    }
}