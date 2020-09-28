package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class SaveMaxPlaytimeSettingUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : SuspendUseCase<SaveMaxPlaytimeSettingUseCase.Params, Unit>() {

    override suspend fun execute(params: Params) {
        userSettingsRepository.saveMaxPlaytime(params.maxHours)
    }

    data class Params(
        val maxHours: Int
    )
}