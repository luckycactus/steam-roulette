package ru.luckycactus.steamroulette.domain.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

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