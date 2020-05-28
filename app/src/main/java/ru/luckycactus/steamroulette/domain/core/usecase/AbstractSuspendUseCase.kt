package ru.luckycactus.steamroulette.domain.core.usecase

abstract class AbstractSuspendUseCase<in Params, Result> {

    internal abstract suspend fun execute(params: Params): Result

    suspend operator fun invoke(params: Params) = execute(params)
}

suspend operator fun <Result> AbstractSuspendUseCase<Unit, Result>.invoke(): Result =
    invoke(Unit)