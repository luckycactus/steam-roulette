package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class SaveMaxPlaytimeSettingUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : SuspendUseCase<SaveMaxPlaytimeSettingUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        userSettingsRepository.saveMaxPlaytime(params.steamId, params.maxHours)
    }

    data class Params(
        val steamId: SteamId,
        val maxHours: Int
    )
}