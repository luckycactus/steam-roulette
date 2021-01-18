package ru.luckycactus.steamroulette.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.File

fun getJson(path: String): String {
    val file = File("src/test/resources/$path")
    return String(file.readBytes())
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