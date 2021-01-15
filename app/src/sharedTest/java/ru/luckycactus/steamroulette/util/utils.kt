package ru.luckycactus.steamroulette.util

import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import retrofit2.HttpException
import ru.luckycactus.steamroulette.data.core.NetworkConnectionException
import ru.luckycactus.steamroulette.data.core.ApiException
import java.io.File
import java.io.IOException

fun getJson(path: String): String {
    val file = File("src/test/resources/$path")
    return String(file.readBytes())
}

suspend fun <T> testCommonNetworkExceptions(
    stubBlock: suspend MockKMatcherScope.() -> T,
    block: suspend () -> Unit
) {
    coEvery(stubBlock) throws mockk<HttpException>()
    try {
        block()
        fail("ServerException expected")
    } catch (e: ApiException) {
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

inline fun <T> CoroutineScope.testFlow(
    flow: Flow<T>,
    block: FlowTestHelper<T>.() -> Unit
) {
    val helper = FlowTestHelper(flow, this)
    try {
        block.invoke(helper)
    } finally {
        helper.close()
    }
}