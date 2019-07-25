package ru.luckycactus.steamroulette.presentation

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

    inline fun handleIfNotHandled(block: (T) -> Unit) {
        getIfNotHandled()?.let { block(it) }
    }

    fun peek(): T = data
}