package ru.luckycactus.steamroulette.domain.common

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
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