package ru.luckycactus.steamroulette.domain.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
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

fun <T> Flow<T>.toConflatedBroadcastChannel(scope: CoroutineScope): ConflatedBroadcastChannel<T> {
    val channel = ConflatedBroadcastChannel<T>()
    scope.launch {
        collect { channel.offer(it) }
    }
    return channel
}

//fun <T> Flow<T>.share(scope: CoroutineScope): Flow<T> =
//    toConflatedBroadcastChannel(scope).asFlow()