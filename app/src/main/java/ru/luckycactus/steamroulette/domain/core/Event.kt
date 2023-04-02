package ru.luckycactus.steamroulette.domain.core

import androidx.lifecycle.Observer

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

@JvmInline
value class EventObserver<T>(
    private inline val onEvent: (T) -> Unit
) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>) {
        event.ifNotHandled { onEvent(it) }
    }

}