package ru.luckycactus.steamroulette.domain.core.usecase

abstract class UseCase<in Params, Result> {

    internal abstract fun execute(params: Params): Result

    open operator fun invoke(params: Params) = execute(params)
}

operator fun <Result> UseCase<Unit, Result>.invoke(): Result =
    invoke(Unit)