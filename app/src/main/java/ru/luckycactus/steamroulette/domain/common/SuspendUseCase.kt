package ru.luckycactus.steamroulette.domain.common

abstract class SuspendUseCase<in Params, Result> {

    internal abstract suspend fun getResult(params: Params): Result

    suspend fun execute(params: Params) = getResult(params)

    suspend operator fun invoke(params: Params) = execute(params)
}

suspend fun <Params, Result> SuspendUseCase<Params?, Result>.execute(): Result =
    execute(null)

suspend operator fun <Params, Result> SuspendUseCase<Params?, Result>.invoke(): Result =
    execute(null)