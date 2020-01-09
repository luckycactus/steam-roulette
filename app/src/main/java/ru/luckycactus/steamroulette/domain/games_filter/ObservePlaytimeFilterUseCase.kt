package ru.luckycactus.steamroulette.domain.games_filter

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import ru.luckycactus.steamroulette.presentation.utils.combine
import javax.inject.Inject

@Reusable
class ObservePlaytimeFilterUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : UseCase<SteamId, LiveData<PlaytimeFilter>>() {

    override fun getResult(params: SteamId): LiveData<PlaytimeFilter> =
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