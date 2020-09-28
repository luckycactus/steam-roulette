package ru.luckycactus.steamroulette.domain.core.usecase

abstract class SuspendUseCase<in Params, Result> {

    protected abstract suspend fun execute(params: Params): Result

    suspend operator fun invoke(params: Params) = execute(params)
}

suspend operator fun <Result> SuspendUseCase<Unit, Result>.invoke(): Result =
    invoke(Unit)