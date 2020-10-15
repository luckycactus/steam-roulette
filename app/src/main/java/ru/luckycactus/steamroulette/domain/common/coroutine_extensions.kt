package ru.luckycactus.steamroulette.domain.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

fun <R> Flow<R>.chunkBuffer(bufferSize: Int): Flow<List<R>> = flow {
    var buffer: MutableList<R>? = null

    collect { value ->
        if (buffer == null) {
            buffer = ArrayList(bufferSize)
        }
        buffer!!.let {
            it.add(value)
            if (it.size == bufferSize) {
                emit(it)
                buffer = null
            }
        }
    }

    buffer?.let {
        if (it.isNotEmpty()) {
            emit(it)
        }
    }
}

fun <T> Flow<T>.stateIn(scope: CoroutineScope, default: T): StateFlow<T> {
    val mutableStateFlow = MutableStateFlow(default)
    scope.launch {
        collect { mutableStateFlow.value = it }
    }
    return mutableStateFlow
}