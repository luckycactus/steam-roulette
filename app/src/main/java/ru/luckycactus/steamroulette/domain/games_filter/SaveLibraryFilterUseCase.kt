package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import javax.inject.Inject
import javax.inject.Named

@Reusable
class SaveLibraryFilterUseCase @Inject constructor(
    @Named("library") private val gamesFilterRepository: GamesFilterRepository
): SuspendUseCase<GamesFilter, Unit>() {

    override suspend fun execute(params: GamesFilter) {
        gamesFilterRepository.saveFilter(params)
    }
}