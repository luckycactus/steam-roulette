package ru.luckycactus.steamroulette.data.core

import retrofit2.HttpException
import java.io.IOException

class NetworkConnectionException(cause: Throwable? = null) : Exception(cause)

class ServerException(cause: Throwable? = null) : Exception(cause)

inline fun <T> wrapCommonNetworkExceptions(block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        throw when (e) {
            is HttpException -> ServerException(e)
            is IOException/*,
            is SocketTimeoutException*/ -> NetworkConnectionException(e)
            else -> e
        }
    }
}