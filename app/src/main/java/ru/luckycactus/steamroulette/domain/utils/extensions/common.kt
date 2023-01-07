package ru.luckycactus.steamroulette.domain.utils.extensions

import kotlin.coroutines.cancellation.CancellationException

val <T> T.exhaustive: T
    get() = this

inline fun <reified T : Throwable, R : Any?> Result<R>.except(): Result<R> =
    onFailure { if (it is T) throw it }

fun <R : Any?> Result<R>.cancellable() = except<CancellationException, R>()