package ru.luckycactus.steamroulette.presentation.utils

import androidx.fragment.app.Fragment
import ru.luckycactus.steamroulette.domain.login.LoginUseCase

interface AnalyticsHelper {
    fun logScreenIfVisibleAndResumed(fragment: Fragment, screen: String)
    fun logSelectContent(type: String, itemId: String)
    fun logLoginAttempt(it: LoginUseCase.Result)
    fun setUserIsLoggingOut()
    fun logClick(button: String)
}