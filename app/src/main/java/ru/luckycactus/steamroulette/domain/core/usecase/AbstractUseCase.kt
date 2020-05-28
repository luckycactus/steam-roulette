package ru.luckycactus.steamroulette.domain.core.usecase

abstract class AbstractUseCase<in Params, Result> {

    internal abstract fun execute(params: Params): Result

    open operator fun invoke(params: Params) = execute(params)
}

operator fun <Result> AbstractUseCase<Unit, Result>.invoke(): Result =
    invoke(Unit)