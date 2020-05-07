package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.UseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class ObservePlaytimeFilterUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : UseCase<SteamId, Flow<PlaytimeFilter>>() {

    override fun getResult(params: SteamId): Flow<PlaytimeFilter> =
        userSettingsRepository.observePlaytimeFilterType(params, PlaytimeFilter.Type.All)
            .combine(
                userSettingsRepository.observeMaxPlaytime(params, 2)
            ) { type, maxPlaytime ->
                when (type) {
                    PlaytimeFilter.Type.All -> PlaytimeFilter.All
                    PlaytimeFilter.Type.NotPlayed -> PlaytimeFilter.NotPlayed
                    PlaytimeFilter.Type.Limited -> PlaytimeFilter.Limited(maxPlaytime)
                }
            }
}