package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.Consts
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject
import javax.inject.Named

@Reusable
class ObserveRouletteMaxHoursUseCase @Inject constructor(
    @Named("roulette") private val rouletteFiltersRepository: GamesFilterRepository
) : UseCase<Unit, Flow<Int>>() {

    override fun execute(params: Unit): Flow<Int> =
        rouletteFiltersRepository.observeMaxHours(Consts.DEFAULT_MAX_HOURS)
}