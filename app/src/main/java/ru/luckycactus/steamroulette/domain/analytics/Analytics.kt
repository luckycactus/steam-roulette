package ru.luckycactus.steamroulette.domain.analytics

interface Analytics {
    fun trackScreen(screen: String?)
    fun track(event: SelectContentEvent)
    fun setUserIsLoggingOut()
}