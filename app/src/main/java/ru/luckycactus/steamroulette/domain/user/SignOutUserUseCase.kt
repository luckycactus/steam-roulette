package ru.luckycactus.steamroulette.domain.user

import ru.luckycactus.steamroulette.data.user.UserRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase

class SignOutUserUseCase(
    private val userRepository: UserRepository
): SuspendUseCase<Unit?, Unit>() {

    override suspend fun getResult(params: Unit?) {
        userRepository.signOut()
        //todo remove games
    }
}