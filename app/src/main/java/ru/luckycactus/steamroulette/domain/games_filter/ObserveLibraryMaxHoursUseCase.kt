package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.Consts
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject
import javax.inject.Named


@Reusable
class ObserveLibraryMaxHoursUseCase @Inject constructor(
    @Named("library") private val filtersRepository: GamesFilterRepository
) : UseCase<Unit, Flow<Int>>() {

    override fun execute(params: Unit): Flow<Int> =
        filtersRepository.observeMaxHours(Consts.DEFAULT_MAX_HOURS)
}