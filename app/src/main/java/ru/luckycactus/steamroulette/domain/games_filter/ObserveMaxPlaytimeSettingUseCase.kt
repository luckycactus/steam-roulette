package ru.luckycactus.steamroulette.domain.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class ObserveMaxPlaytimeSettingUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : AbstractUseCase<Unit, Flow<Int>>() {

    override fun execute(params: Unit): Flow<Int> =
        userSettingsRepository.observeMaxPlaytime(2)
}