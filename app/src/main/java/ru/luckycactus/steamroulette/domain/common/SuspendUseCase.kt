package ru.luckycactus.steamroulette.domain.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.luckycactus.steamroulette.domain.entity.CachePolicy

abstract class SuspendUseCase<in Params, Result> {

    internal abstract suspend fun getResult(params: Params): Result

    suspend fun execute(params: Params) = getResult(params)

    suspend operator fun invoke(params: Params) = execute(params)

    protected fun getCacheThenRemote(block: suspend (CachePolicy) -> Result): Flow<Result> =
        flow {
            try {
                emit(block(CachePolicy.ONLY_CACHE))
            } catch (e: Exception) {
                //do nothing
            }
            emit(block(CachePolicy.REMOTE))
        }
}

suspend fun <Params, Result> SuspendUseCase<Params?, Result>.execute(): Result =
    execute(null)

suspend operator fun <Params, Result> SuspendUseCase<Params?, Result>.invoke(): Result =
    execute(null)
