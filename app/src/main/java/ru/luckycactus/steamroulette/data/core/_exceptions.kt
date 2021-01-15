package ru.luckycactus.steamroulette.data.core

import retrofit2.HttpException
import java.io.IOException

class NetworkConnectionException(cause: Throwable? = null) : Exception(cause)

class ApiException(cause: Throwable? = null, val code: Int) : Exception(cause)

class UnexpectedException(cause: Throwable? = null) : Exception(cause)

inline fun <T> wrapCommonNetworkExceptions(block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        throw mapNetworkException(e)
    }
}

fun mapNetworkException(e: Throwable) = when (e) {
    is IOException -> NetworkConnectionException(e)
    is HttpException -> ApiException(e, e.code())
    is UnexpectedException -> e
    else -> UnexpectedException(e)
}