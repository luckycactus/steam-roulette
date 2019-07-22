package ru.luckycactus.steamroulette.domain.common

abstract class UseCase<in Params, Result> {

    internal abstract fun getResult(params: Params): Result

    fun execute(params: Params) = getResult(params)

    operator fun invoke(params: Params) = execute(params)
}

fun <Params, Result> UseCase<Params?, Result>.execute(): Result =
    execute(null)

operator fun <Params, Result> UseCase<Params?, Result>.invoke(): Result =
    execute(null)