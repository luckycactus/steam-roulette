package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class SaveMaxPlaytimeSettingUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : AbstractSuspendUseCase<SaveMaxPlaytimeSettingUseCase.Params, Unit>() {

    override suspend fun execute(params: Params) {
        userSettingsRepository.saveMaxPlaytime(params.steamId, params.maxHours)
    }

    data class Params(
        val steamId: SteamId,
        val maxHours: Int
    )
}