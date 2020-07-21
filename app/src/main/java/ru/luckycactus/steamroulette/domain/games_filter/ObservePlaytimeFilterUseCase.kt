package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class ObservePlaytimeFilterUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : AbstractUseCase<Unit, Flow<PlaytimeFilter>>() {

    override fun execute(params: Unit): Flow<PlaytimeFilter> =
        userSettingsRepository.observePlaytimeFilterType(PlaytimeFilter.Type.All)
            .combine(
                userSettingsRepository.observeMaxPlaytime(2)
            ) { type, maxPlaytime ->
                when (type) {
                    PlaytimeFilter.Type.All -> PlaytimeFilter.All
                    PlaytimeFilter.Type.NotPlayed -> PlaytimeFilter.NotPlayed
                    PlaytimeFilter.Type.Limited -> PlaytimeFilter.Limited(maxPlaytime)
                }
            }
}