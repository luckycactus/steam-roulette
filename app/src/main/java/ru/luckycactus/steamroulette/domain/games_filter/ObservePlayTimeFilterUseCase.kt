package ru.luckycactus.steamroulette.domain.games_filter

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class ObservePlayTimeFilterUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : UseCase<SteamId, LiveData<EnPlayTimeFilter>>() {

    override fun getResult(params: SteamId): LiveData<EnPlayTimeFilter> =
        userSettingsRepository.observePlayTimeFilter(params, EnPlayTimeFilter.All)

}

