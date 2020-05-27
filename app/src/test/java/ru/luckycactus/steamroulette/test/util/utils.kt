package ru.luckycactus.steamroulette.test.util

import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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

fun getJson(path: String): String {
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

suspend fun <T> testCommonNetworkExceptions(
    stubBlock: suspend MockKMatcherScope.() -> T,
    block: suspend () -> Unit
) {
    coEvery(stubBlock) throws mockk<HttpException>()
    try {
        block()
        fail("ServerException expected")
    } catch (e: ServerException) {
        assertEquals(HttpException::class.java, e.cause?.javaClass)
    }

    coEvery(stubBlock) throws IOException("exception")
    try {
        block()
        fail("NetworkConnectionException expected")
    } catch (e: NetworkConnectionException) {
        assertEquals(IOException::class.java, e.cause?.javaClass)
    }

    coEvery(stubBlock) throws RuntimeException()
    try {
        block()
        fail("RuntimeException expected")
    } catch (e: RuntimeException) {
    }
}