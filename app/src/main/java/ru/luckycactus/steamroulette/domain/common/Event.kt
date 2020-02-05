package ru.luckycactus.steamroulette.domain.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.map

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

fun <T> LiveData<T>.toEvent(): LiveData<Event<T>> = map {
    Event(it)
}