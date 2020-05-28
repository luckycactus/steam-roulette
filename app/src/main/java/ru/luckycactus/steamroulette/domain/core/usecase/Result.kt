package ru.luckycactus.steamroulette.domain.core.usecase

sealed class Result<out T> {
    data class Success<out T>(
        val data: T
    ) : Result<T>()

    data class Error(val cause: Exception) : Result<Nothing>()
}

fun <T> Result<T>.successOr(fallback: T): T {
    return (this as? Result.Success<T>)?.data ?: fallback
}

fun <T> Result<T>.requireSuccess(): T {
    return (this as? Result.Success<T>)?.data
        ?: throw IllegalStateException("$this is not ${Result.Success::class.simpleName}")
}

val <T> Result<T>.data: T?
    get() = (this as? Result.Success)?.data