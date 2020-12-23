package ru.luckycactus.steamroulette.domain.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Debouncer(
    private val coroutineScope: CoroutineScope
) {
    private var job: Job? = null

    fun debounce(timeMillis: Long, block: () -> Unit): Job {
        job?.cancel()
        return coroutineScope.launch {
            delay(timeMillis)
            block()
        }.also {
            job = it
        }
    }

}

fun CoroutineScope.newDebouncer() = Debouncer(this)
