package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

@Reusable
class ObserveRouletteFilterUseCase @Inject constructor(
    private val rouletteFiltersRepository: RouletteFiltersRepository
) : UseCase<Unit, Flow<PlaytimeFilter>>() {

    override fun execute(params: Unit): Flow<PlaytimeFilter> =
        rouletteFiltersRepository.observePlaytimeFilter(PlaytimeFilter.Type.All, 2)
}