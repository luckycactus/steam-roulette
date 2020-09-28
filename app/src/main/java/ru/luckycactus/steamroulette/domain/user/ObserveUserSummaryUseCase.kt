package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

@Reusable
class ObserveUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<Unit, Flow<UserSummary>>() {

    override fun execute(params: Unit): Flow<UserSummary> =
        userRepository.observeUserSummary()
}