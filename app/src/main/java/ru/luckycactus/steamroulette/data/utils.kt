package ru.luckycactus.steamroulette.data

import retrofit2.HttpException
import ru.luckycactus.steamroulette.domain.exception.NetworkConnectionException
import ru.luckycactus.steamroulette.domain.exception.ServerException
import java.io.IOException
import java.net.SocketTimeoutException

inline fun <T> wrapCommonNetworkExceptions(block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        throw when (e) {
            is HttpException -> ServerException(e)
            is IOException,
            is SocketTimeoutException -> NetworkConnectionException(e)
            else -> e
        }
    }
}