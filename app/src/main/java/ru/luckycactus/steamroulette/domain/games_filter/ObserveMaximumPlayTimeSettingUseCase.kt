package ru.luckycactus.steamroulette.domain.games_filter

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class ObserveMaximumPlayTimeSettingUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : UseCase<SteamId, LiveData<Int>>() {

    override fun getResult(params: SteamId): LiveData<Int> =
        userSettingsRepository.observeMaxPlaytime(params, 2)
}