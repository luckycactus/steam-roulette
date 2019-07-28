package ru.luckycactus.steamroulette.domain.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import ru.luckycactus.steamroulette.domain.CachePolicy

abstract class SuspendUseCase<in Params, Result> {

    internal abstract suspend fun getResult(params: Params): Result

    suspend fun execute(params: Params) = getResult(params)

    suspend operator fun invoke(params: Params) = execute(params)

    @ExperimentalCoroutinesApi
    protected fun CoroutineScope.getCacheThenRemote(block: suspend (CachePolicy) -> Result): ReceiveChannel<Result> = produce {
        send(block(CachePolicy.ONLY_CACHE))
        send(block(CachePolicy.REMOTE))
    }
}

suspend fun <Params, Result> SuspendUseCase<Params?, Result>.execute(): Result =
    execute(null)

suspend operator fun <Params, Result> SuspendUseCase<Params?, Result>.invoke(): Result =
    execute(null)
