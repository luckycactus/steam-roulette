package ru.luckycactus.steamroulette.domain.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

@UseExperimental(InternalCoroutinesApi::class)
fun <R> Flow<R>.chunkBuffer(bufferSize: Int) =
    object : Flow<List<R>> {

        private var buffer = AtomicReference<ArrayList<R>>()

        init {
            createBuffer()
        }

        override suspend fun collect(collector: FlowCollector<List<R>>) {
            this@chunkBuffer.collect {
                buffer.get().run {
                    add(it)
                    if (size == bufferSize) {
                        collector.emit(this)
                        createBuffer()
                    }
                }
            }

            if (buffer.get().isNotEmpty()) {
                collector.emit(buffer.get()!!)
            }
        }

        private fun createBuffer() {
            buffer.set(ArrayList(bufferSize))
        }
    }

fun <T> Flow<T>.toStateFlow(scope: CoroutineScope, default: T): StateFlow<T> {
    val mutableStateFlow = MutableStateFlow(default)
    scope.launch {
        collect { mutableStateFlow.value = it }
    }
    return mutableStateFlow
}

fun <T> Flow<T?>.switchNullsToEmpty(): Flow<T> = flatMapLatest {
    it?.let { flowOf(it) } ?: emptyFlow()
}