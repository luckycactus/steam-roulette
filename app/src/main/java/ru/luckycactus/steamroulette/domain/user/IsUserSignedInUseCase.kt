package ru.luckycactus.steamroulette.domain.user

import ru.luckycactus.steamroulette.data.user.UserRepository
import ru.luckycactus.steamroulette.domain.common.UseCase

class IsUserSignedInUseCase(
    private val userRepository: UserRepository
): UseCase<Unit?, Boolean>() {

    override fun getResult(params: Unit?): Boolean {
        return userRepository.isUserSignedIn()
    }
}