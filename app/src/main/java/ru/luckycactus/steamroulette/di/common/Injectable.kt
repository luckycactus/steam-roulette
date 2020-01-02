package ru.luckycactus.steamroulette.di.common

/**
 * Implement this interface by Fragment or Activity and
 * InjectionManager will call inject() automatically
 */
interface Injectable {
    fun inject()
}