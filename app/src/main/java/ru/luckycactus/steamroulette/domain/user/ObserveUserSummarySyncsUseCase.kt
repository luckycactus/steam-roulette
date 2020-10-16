package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject

class ObserveUserSummarySyncsUseCase @Inject constructor(
    private val userRepository: UserRepository
): UseCase<Unit, Flow<Long>>() {

    override fun execute(params: Unit): Flow<Long> {
        return userRepository.observeSummaryUpdates()
    }
}