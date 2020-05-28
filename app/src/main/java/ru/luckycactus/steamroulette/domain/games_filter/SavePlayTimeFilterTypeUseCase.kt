package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class SavePlayTimeFilterTypeUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : AbstractUseCase<SavePlayTimeFilterTypeUseCase.Params, Unit>() {

    override fun execute(params: Params) {
        userSettingsRepository.savePlayTimeFilterType(params.steamId, params.playtimeFilterType)
    }

    data class Params(
        val steamId: SteamId,
        val playtimeFilterType: PlaytimeFilter.Type
    )
}