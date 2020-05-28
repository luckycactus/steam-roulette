package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class ObserveMaxPlaytimeSettingUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : AbstractUseCase<SteamId, Flow<Int>>() {

    override fun execute(params: SteamId): Flow<Int> =
        userSettingsRepository.observeMaxPlaytime(params, 2)
}