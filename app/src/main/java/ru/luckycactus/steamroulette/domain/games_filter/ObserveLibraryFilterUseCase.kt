package ru.luckycactus.steamroulette.domain.games_filter

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

class ObserveLibraryFilterUseCase @Inject constructor(
    private val filterRepository: LibraryFilterRepository
) {
    operator fun invoke(): Flow<GamesFilter> {
        return filterRepository.observeFilter(GamesFilter.onlyVisible())
    }
}