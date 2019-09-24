package ru.luckycactus.steamroulette.domain.user

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.UserSummary

class ObserveCurrentUserSummaryUseCase(
    private val userRepository: UserRepository
) : UseCase<Unit?, LiveData<UserSummary?>>() {

    override fun getResult(params: Unit?): LiveData<UserSummary?> =
        userRepository.observeCurrentUserSummary()
}