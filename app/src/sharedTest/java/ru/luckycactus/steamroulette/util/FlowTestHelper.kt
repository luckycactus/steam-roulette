package ru.luckycactus.steamroulette.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.junit.Assert

class FlowTestHelper<T>(private val flow: Flow<T>, scope: CoroutineScope) {
    private val channel = Channel<T>()

    private val job = scope.launch {
        flow.collect {
            channel.send(it)
        }
    }

    suspend fun nextValue() = channel.receive()

    suspend fun assertNextValue(expected: T) {
        Assert.assertEquals(expected, nextValue())
    }

    fun assertEmpty() {
        Assert.assertTrue(channel.isEmpty)
    }

    fun close() {
        job.cancel()
        channel.close()
    }
}