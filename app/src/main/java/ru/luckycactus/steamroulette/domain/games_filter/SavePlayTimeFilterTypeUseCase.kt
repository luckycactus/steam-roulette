package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class SavePlayTimeFilterTypeUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : UseCase<SavePlayTimeFilterTypeUseCase.Params, Unit>() {

    override fun getResult(params: Params) {
        userSettingsRepository.savePlayTimeFilterType(params.steamId, params.playtimeFilterType)
    }

    data class Params(
        val steamId: SteamId,
        val playtimeFilterType: PlaytimeFilter.Type
    )
}