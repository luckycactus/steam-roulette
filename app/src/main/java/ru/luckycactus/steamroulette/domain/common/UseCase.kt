package ru.luckycactus.steamroulette.domain.common

abstract class UseCase<in Params, Result> {

    internal abstract fun getResult(params: Params): Result

    fun execute(params: Params) = getResult(params)

    operator fun invoke(params: Params) = execute(params)
}

fun <Result> UseCase<Unit, Result>.execute(): Result =
    execute(Unit)

operator fun <Result> UseCase<Unit, Result>.invoke(): Result =
    execute(Unit)