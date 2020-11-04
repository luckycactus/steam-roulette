package ru.luckycactus.steamroulette.domain.games_filter

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.Consts
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject

class ObserveLibraryMaxHoursUseCase @Inject constructor(
    private val filtersRepository: LibraryFilterRepository
) : UseCase<Unit, Flow<Int>>() {

    override fun execute(params: Unit): Flow<Int> =
        filtersRepository.observeMaxHours(Consts.DEFAULT_MAX_HOURS)
}