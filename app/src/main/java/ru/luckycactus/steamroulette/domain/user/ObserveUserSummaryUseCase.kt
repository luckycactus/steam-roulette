package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

class ObserveUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<UserSummary> = userRepository.observeUserSummary()
}