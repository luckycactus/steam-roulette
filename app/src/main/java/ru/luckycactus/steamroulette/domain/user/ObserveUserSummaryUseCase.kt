package ru.luckycactus.steamroulette.domain.user

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

@Reusable
class ObserveUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<SteamId, LiveData<UserSummary>>() {

    override fun getResult(params: SteamId): LiveData<UserSummary> =
        userRepository.observeUserSummary(params)
}