package ru.luckycactus.steamroulette.domain.common

abstract class SuspendUseCase<in Params, Result> {

    internal abstract suspend fun getResult(params: Params): Result

    suspend fun execute(params: Params) = getResult(params)

    suspend operator fun invoke(params: Params) = execute(params)
}

suspend fun <Result> SuspendUseCase<Unit, Result>.execute(): Result =
    execute(Unit)

suspend operator fun <Result> SuspendUseCase<Unit, Result>.invoke(): Result =
    execute(Unit)