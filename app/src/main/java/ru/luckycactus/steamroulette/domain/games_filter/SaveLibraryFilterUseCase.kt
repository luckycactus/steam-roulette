package ru.luckycactus.steamroulette.domain.games_filter

import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

class SaveLibraryFilterUseCase @Inject constructor(
    private val filterRepository: LibraryFilterRepository
) {
    suspend operator fun invoke(params: GamesFilter) {
        filterRepository.saveFilter(params)
    }
}