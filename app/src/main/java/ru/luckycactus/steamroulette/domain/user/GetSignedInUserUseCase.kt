package ru.luckycactus.steamroulette.domain.user

import ru.luckycactus.steamroulette.data.user.UserRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase

class GetSignedInUserUseCase(
    private val userRepository: UserRepository
): SuspendUseCase<GetSignedInUserUseCase.Params, UserSummary>() {

    override suspend fun getResult(params: Params): UserSummary {
        return userRepository.getSignedInUserSummary(params.reload)
    }

    data class Params(
        val reload: Boolean
    )
}