package ru.luckycactus.steamroulette.domain.games_filter

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

class ObserveLibraryFilterUseCase @Inject constructor(
    private val filterRepository: LibraryFilterRepository
): UseCase<Unit, Flow<GamesFilter>>() {

    override fun execute(params: Unit): Flow<GamesFilter> {
        return filterRepository.observeFilter(GamesFilter.withoutHidden())
    }

}