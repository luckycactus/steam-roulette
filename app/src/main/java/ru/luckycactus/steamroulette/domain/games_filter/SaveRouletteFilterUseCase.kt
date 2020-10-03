package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

@Reusable
class SaveRouletteFilterUseCase @Inject constructor(
    private val rouletteFiltersRepository: RouletteFiltersRepository
) : UseCase<GamesFilter, Unit>() {

    override fun execute(filter: GamesFilter) {
        //todo library
        rouletteFiltersRepository.savePlaytimeFilter(filter.playtime)
    }
}