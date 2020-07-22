package ru.luckycactus.steamroulette.domain.user.entity

import ru.luckycactus.steamroulette.domain.UserNotLoggedInException
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) {
    val currentUser
        get() = userSessionRepository.currentUser

    val isUserLoggedIn
        get() = userSessionRepository.isUserLoggedIn()


    fun requireCurrentUser() = currentUser ?: throw UserNotLoggedInException()

    fun observeCurrentUser() = userSessionRepository.observeCurrentUser()
}