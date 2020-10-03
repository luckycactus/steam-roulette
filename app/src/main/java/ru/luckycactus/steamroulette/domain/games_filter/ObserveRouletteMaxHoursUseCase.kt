package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject

@Reusable
class ObserveRouletteMaxHoursUseCase @Inject constructor(
    private val rouletteFiltersRepository: RouletteFiltersRepository
) : UseCase<Unit, Flow<Int>>() {

    // todo library move 2 to consts
    override fun execute(params: Unit): Flow<Int> =
        rouletteFiltersRepository.observeMaxPlaytime(2)
}