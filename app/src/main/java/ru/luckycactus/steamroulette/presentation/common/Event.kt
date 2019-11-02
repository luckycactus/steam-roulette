package ru.luckycactus.steamroulette.presentation.common

class Event<out T>(
    private val data: T
) {

    var handled: Boolean = false
        private set

    fun getIfNotHandled(): T? =
        if (handled) {
            null
        } else {
            handled = true
            data
        }

    inline fun ifNotHandled(block: (T) -> Unit) {
        getIfNotHandled()?.let { block(it) }
    }

    fun peek(): T = data
}