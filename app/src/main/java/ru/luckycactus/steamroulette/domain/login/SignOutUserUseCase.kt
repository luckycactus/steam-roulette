package ru.luckycactus.steamroulette.domain.login

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.user.UserRepository

class SignOutUserUseCase(
    private val userRepository: UserRepository
): SuspendUseCase<Unit?, Unit>() {

    override suspend fun getResult(params: Unit?) {
        userRepository.signOut()
        //todo remove games
    }
}