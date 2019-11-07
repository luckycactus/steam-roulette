package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class SavePlayTimeFilterUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : UseCase<SavePlayTimeFilterUseCase.Params, Unit>() {

    override fun getResult(params: Params) {
        userSettingsRepository.savePlayTimeFilter(params.steamId, params.playTimeFilter)
    }

    data class Params(
        val steamId: SteamId,
        val playTimeFilter: EnPlayTimeFilter
    )
}