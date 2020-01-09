package ru.luckycactus.steamroulette.di.core

/**
 * Implement this interface by Fragment or Activity and
 * InjectionManager will call inject() automatically
 */
interface Injectable {
    fun inject()
}