package ru.luckycactus.steamroulette.test.util

import com.nhaarman.mockitokotlin2.given
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import retrofit2.HttpException
import ru.luckycactus.steamroulette.data.core.NetworkConnectionException
import ru.luckycactus.steamroulette.data.core.ServerException
import java.io.File
import java.io.IOException

fun getJson(path : String) : String {
    // Load the JSON response
    val file = File("src/test/resources/$path")
    return String(file.readBytes())
}

fun <T> Flow<T>.checkValues(
    expectedValues: List<T>,
    testBody: suspend TestCoroutineScope.() -> Unit
) = runBlockingTest {
    val actualValues = mutableListOf<T>()
    val job = launch {
        collect { actualValues.add(it) }
    }

    testBody()

    assertEquals(expectedValues, actualValues)
    job.cancel()
}

suspend fun testCommonNetworkExceptions(methodCall: Any, block: suspend () -> Unit) {
    given(methodCall).willThrow(HttpException::class.java)
    try {
        block()
        fail("ServerException expected")
    } catch (e: ServerException) {
        assertEquals(HttpException::class.java, e.cause?.javaClass)
    }

    given(methodCall).willAnswer { throw IOException("exception") }
    try {
        block()
        fail("NetworkConnectionException expected")
    } catch (e: NetworkConnectionException) {
        assertEquals(IOException::class.java, e.cause?.javaClass)
    }

    given(methodCall).willThrow(RuntimeException::class.java)
    try {
        block()
        fail("RuntimeException expected")
    } catch (e: RuntimeException) { }
}